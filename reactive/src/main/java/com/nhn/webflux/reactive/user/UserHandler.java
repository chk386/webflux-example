package com.nhn.webflux.reactive.user;

import com.nhn.webflux.reactive.user.request.UserRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;

/**
 * @author haekyu cho
 */
@Component
public class UserHandler {

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  Mono<ServerResponse> getUser(ServerRequest request) {
    String id = request.pathVariable("id");
    String name = request.queryParam("name")
                         .orElse("");
    List<String> clientIds = request.headers()
                                    .header("clientId");

    String clientId = clientIds.isEmpty() ? "" : clientIds.get(0);
    logger.debug("clientId : {}, id : {}, name : {}", clientId, id, name);

    return ServerResponse.ok()
                         .contentType(APPLICATION_JSON)
                         .bodyValue(new UserRequest(Long.parseLong(id), name, name + "@nhn.com", 0))
                         .switchIfEmpty(ServerResponse.noContent()
                                                      .build());
  }

  Mono<ServerResponse> createUser(ServerRequest request) {
    return request.bodyToMono(UserRequest.class)
                  .doOnSubscribe(v -> logger.debug("doOnSubScribe 실행"))
                  .doOnNext(user -> logger.debug("created a user : {}", user))
                  .doOnNext(user -> {
                    if (user.getId() != 0) {
                      throw new ServerWebInputException("유저 등록시 id는 0이여야 합니다.");
                    }
                  })
                  .doOnError(e -> logger.error("유저를 저장하는 도중 오류가 발생하였습니다.", e))
                  .flatMap(user -> Mono.just(new UserRequest(9999, user.getName(), user.getEmail(), 0)))
                  .log()
                  .flatMap(user -> {
                    final var uriVariables = Map.of("id", user.getId(), "name", user.getName());
                    URI uri = UriComponentsBuilder.newInstance()
                                                  .path("/users/{id}?name={name}")
                                                  .encode()
                                                  .buildAndExpand(uriVariables)
                                                  .toUri();

                    return ServerResponse.created(uri)
                                         .contentType(APPLICATION_JSON)
                                         .bodyValue(user);
                  });
  }

  Mono<ServerResponse> modifyUser(ServerRequest request) {
    return request.bodyToMono(UserRequest.class)
                  .flatMap(user -> ServerResponse.noContent()
                                                 .build());
  }

  Mono<ServerResponse> bulkUsers(ServerRequest req) {
    return req.body(BodyExtractors.toMultipartData())
              .flatMap(parts -> {
                Map<String, Part> map = parts.toSingleValueMap();
                Part file = map.get("sample.txt");

                logger.info("uploaded id : {}", file.name());

                AtomicInteger atomicInteger = new AtomicInteger(0);

                var flux = Flux.create(sink -> file.content()
                                                   .doFinally(type -> sink.complete())
                                                   .subscribe(buf -> {
                                                     String received = buf.toString(Charset.defaultCharset());
                                                     Arrays.stream(received.split("\n"))
                                                           .forEach(sink::next);
                                                   }))
//                               .buffer(10)
//                               .delayElements(Duration.ofSeconds(1))
//                               .flatMap(v -> Flux.fromStream(v.stream()
//                                                              .map(text -> (String)text)
//                                                              .map(String::toUpperCase)
//                                                              .map(text -> text.replace("-", "*"))
//                                                              .map(text -> atomicInteger.incrementAndGet() + " line -> "
//                                                                           + text + " (" + text.length() + ") Bytes")))
                               .doOnNext(text -> logger.debug("client 송신\n{}", text));

                return ServerResponse.ok()
                                     .contentType(TEXT_EVENT_STREAM)
                                     .body(flux, String.class);
              });
  }
}