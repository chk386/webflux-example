package com.nhn.webflux.reactive.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import io.netty.buffer.ByteBufAllocator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN;

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
                             .bodyValue(new User(Long.parseLong(id), name, name + "@nhn.com", 0))
                             .switchIfEmpty(ServerResponse.noContent()
                                                          .build());
    }

    Mono<ServerResponse> createUser(ServerRequest request) {
        return request.bodyToMono(User.class)
                      .doOnSubscribe(v -> logger.debug("doOnSubScribe 실행"))
                      .doOnNext(user -> logger.debug("created a user : {}", user))
                      .doOnNext(user -> {
                          if (user.getId() != 0) {
                              throw new ServerWebInputException("유저 등록시 id는 0이여야 합니다.");
                          }
                      })
                      .doOnError(e -> logger.error("유저를 저장하는 도중 오류가 발생하였습니다.", e))
                      .flatMap(user -> Mono.just(new User(9999, user.getName(), user.getEmail(), 0)))
                      .log()
                      .flatMap(user -> {
                          final Map<String, Object> uriVariables = Map.of("id", user.getId(), "name", user.getName());
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

    Mono<ServerResponse> bulkUsers(ServerRequest request) {
        return request.body(BodyExtractors.toMultipartData())
                      .flatMap(parts -> {
                          Map<String, Part> map = parts.toSingleValueMap();
                          Part file = map.get("files");

                          logger.info("uploaded file name : {}", file.name());

                          Flux<String> flux = Flux.create(fluxSink -> file.content()
                                                                          .doOnNext(buf -> logger.info(getMsg(buf)))
                                                                          .doFinally(type -> logger.info(
                                                                              "final signal type : {}",
                                                                              type.toString()))
                                                                          .subscribe(buf -> fluxSink.next(getMsg(buf))));

                          return ServerResponse.ok()
                                               .contentType(TEXT_PLAIN)
                                               .body(flux, String.class);
                      });
    }

    private String getMsg(DataBuffer buf) {
        return StandardCharsets.UTF_8.decode(buf.asByteBuffer())
                                     .toString();
    }
}