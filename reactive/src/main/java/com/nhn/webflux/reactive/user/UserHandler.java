package com.nhn.webflux.reactive.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow;

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
                      .doOnSubscribe(v -> logger.debug("doOnSubScribe"))
                      .doOnNext(user -> logger.debug("created a user : {}", user))
                      .doOnNext(user -> {
                          if (user.getId() != 0) {
                              throw new ServerWebInputException("유저 등록시 id는 0이여야 합니다.");
                          }
                      })
                      .doOnError(e -> logger.error("유저 똑바로 넣어주세요!"))
                      .flatMap(user -> Mono.just(new User(9999, user.getName(), user.getEmail(), 0)))
                      .log()
                      .flatMap(user -> {
                          URI uri = UriComponentsBuilder.newInstance()
                                                        .path("/users/{id}?name={name}")
                                                        .encode()
                                                        .buildAndExpand(Map.of("id", user.getId(), "name", user.getName()))
                                                        .toUri();

                          return ServerResponse.created(uri)
                                               .contentType(APPLICATION_JSON)
                                               .bodyValue(user);
                      });
    }
}