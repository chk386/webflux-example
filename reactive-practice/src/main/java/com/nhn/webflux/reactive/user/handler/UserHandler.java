package com.nhn.webflux.reactive.user.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

/**
 * @author haekyu cho
 */
@Component
public class UserHandler {

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  public Mono<ServerResponse> getUser(ServerRequest request) {
    // todo: 코드작성
    return null;
  }

  public Mono<ServerResponse> createUser(ServerRequest request) {
    // todo: 코드작성
    return null;
  }
}