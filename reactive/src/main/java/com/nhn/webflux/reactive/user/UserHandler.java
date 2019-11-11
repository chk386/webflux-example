package com.nhn.webflux.reactive.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;

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
}