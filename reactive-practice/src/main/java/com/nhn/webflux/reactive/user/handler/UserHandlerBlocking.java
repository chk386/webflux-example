package com.nhn.webflux.reactive.user.handler;

import com.nhn.webflux.reactive.user.service.UserService;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

/**
 * <a href="https://projectreactor.io/docs/core/release/reference/#faq.wrap-blocking">wrap blocking</a>
 */
@Component
public class UserHandlerBlocking {

  private final UserService userService;

  public UserHandlerBlocking(UserService userService) {
    this.userService = userService;
  }

  public Mono<ServerResponse> getUser(ServerRequest request) {
    // todo: 코드작성
    return null;
  }

  public Mono<ServerResponse> createUser(ServerRequest request) {
    // todo: 코드작성
    return null;
  }
}
