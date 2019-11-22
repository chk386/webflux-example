package com.nhn.webflux.reactive.user.handler;

import com.nhn.webflux.reactive.user.model.UserRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
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

  public Mono<ServerResponse> getUser(ServerRequest request) {
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

  public Mono<ServerResponse> createUser(ServerRequest request) {
    return request.bodyToMono(UserRequest.class)
                  .doOnSubscribe(v -> logger.debug("doOnSubScribe 실행"))
                  .doOnNext(user -> logger.debug("created a user : {}", user))
                  .doOnNext(user -> {
                    if (user.getId() != 0) {
                      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                  })
                  .doOnError(e -> logger.error("유저를 저장하는 도중 오류가 발생하였습니다.", e))
                  .flatMap(user -> Mono.just(new UserRequest(9999, user.getName(), user.getEmail(), 0)))
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

  public Mono<ServerResponse> modifyUser(ServerRequest request) {
    return request.bodyToMono(UserRequest.class)
                  .flatMap(user -> ServerResponse.noContent()
                                                 .build());
  }

  public Mono<ServerResponse> bulkUsers(ServerRequest req) {
    return req.body(BodyExtractors.toMultipartData())
              .flatMap(parts -> {
                Map<String, Part> map = parts.toSingleValueMap();
                Part file = map.get("sample.txt");

                logger.info("uploaded id : {}", file.name());

                AtomicInteger atomicInteger = new AtomicInteger(0);

                var flux = file.content()
                               .flatMap(buf -> {
                                 String received = buf.toString(Charset.defaultCharset());
                                 logger.info("recevied data\n{}", received);
                                 return Flux.fromStream(Arrays.stream(received.split("\n")));
                               })
                               .buffer(6)
                               .delayElements(Duration.ofMillis(500))
                               .flatMapSequential(v -> Flux.fromStream(v.stream()
                                                                        .map(String::toUpperCase)
                                                                        .map(text -> text.replace("-", "*"))
                                                                        .map(text -> atomicInteger.incrementAndGet()
                                                                                     + " line -> " + text + " ("
                                                                                     + text.length() + ") Bytes")))
                               .log();

                return ServerResponse.ok()
                                     .contentType(TEXT_EVENT_STREAM)
                                     .body(flux, String.class);
              });
  }
}