package com.nhn.webflux.reactive.user.handler;

import com.nhn.webflux.reactive.user.entity.User;
import com.nhn.webflux.reactive.user.model.UserRequest;
import com.nhn.webflux.reactive.user.service.UserService;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
    final String id = request.pathVariable("id");

    return ServerResponse.ok()
                  .body(userService.getUser(Long.valueOf(id).intValue()), User.class);
  }

  public Mono<ServerResponse> createUser(ServerRequest request) {

    return request.bodyToMono(UserRequest.class)
                  .flatMap(userRequest -> ServerResponse.ok()
                                                        .body(Mono.fromCallable(() -> userService.save(userRequest))
                                                                  .subscribeOn(Schedulers.boundedElastic()),
                                                              User.class));
  }
}
