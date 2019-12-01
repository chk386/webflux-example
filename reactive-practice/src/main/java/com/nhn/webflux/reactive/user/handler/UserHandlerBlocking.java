package com.nhn.webflux.reactive.user.handler;

import com.nhn.webflux.reactive.user.entity.User;
import com.nhn.webflux.reactive.user.model.UserRequest;
import com.nhn.webflux.reactive.user.model.UserResponse;
import com.nhn.webflux.reactive.user.service.UserService;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;

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
    final long id = Long.parseLong(request.pathVariable("id"));

    return ServerResponse.ok()
                         .body(userService.getUser(id), UserResponse.class);

  }

  public Mono<ServerResponse> createUser(ServerRequest request) {
    return request.bodyToMono(UserRequest.class)
                  .doOnNext(userRequest -> {
                    if (userRequest.getId() != 0) {
                      throw new ServerWebInputException("id는 0이여야 합니다.");
                    }
                  })
                  // Mono<UserRequest> -> Mono<ServerResponse>
                  .flatMap(userRequest -> ServerResponse.ok()
                                                        .body(userService.save(userRequest), User.class));
  }
}
