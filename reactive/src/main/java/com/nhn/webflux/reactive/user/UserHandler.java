package com.nhn.webflux.reactive.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;

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
                             .bodyValue(new User(Long.parseLong(id), name, name + "@nhn.com", 0));
    }

    Mono<ServerResponse> createUser(ServerRequest request) {
        return request.bodyToMono(User.class)
                      .doOnNext(user -> logger.debug("created a user : {}", user))
                      .flatMap(user -> {
                          URI uri = UriComponentsBuilder.newInstance()
                                                        .scheme("http")
                                                        .host("localhost")
                                                        .port("8080")
                                                        .path("/users/{id}")
                                                        .queryParam("name", user.getName())
                                                        .buildAndExpand(Collections.singletonMap("id", user.getId()))
                                                        .toUri();

                          return ServerResponse.created(uri)
                                               .contentType(APPLICATION_JSON)
                                               .bodyValue(user);
                      });
    }
}