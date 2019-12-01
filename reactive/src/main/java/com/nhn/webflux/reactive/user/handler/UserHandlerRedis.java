package com.nhn.webflux.reactive.user.handler;

import com.nhn.webflux.reactive.user.entity.User;
import com.nhn.webflux.reactive.user.model.UserRequest;
import com.nhn.webflux.reactive.user.repository.UserRepository;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import io.lettuce.core.RedisCommandExecutionException;
import reactor.core.publisher.Mono;

@Component
public class UserHandlerRedis {

  private final ReactiveRedisTemplate<String, User> template;
  private final UserRepository userRepository;

  public UserHandlerRedis(ReactiveRedisTemplate<String, User> template, UserRepository userRepository) {
    this.template = template;
    this.userRepository = userRepository;
  }

  public Mono<ServerResponse> getUser(ServerRequest request) {
    var id = request.pathVariable("id");
    var mono = template.opsForValue()
                       .get(id)
                       .switchIfEmpty(Mono.fromCallable(() -> userRepository.findById(Long.parseLong(id))
                                                                            .orElseThrow(Exception::new))
                                          .flatMap(this::setUserToCache));

    return ServerResponse.ok()
                         .body(mono, User.class);
  }

  public Mono<ServerResponse> createUser(ServerRequest request) {
    return request.bodyToMono(UserRequest.class)
                  .map(this::toUser)
                  .flatMap(user -> ServerResponse.ok()
                                                 .body(setUserToCache(user), User.class));
  }

  private User toUser(UserRequest userRequest) {
    User user = new User();
    user.setId(userRequest.getId());
    user.setName(userRequest.getName());
    user.setEmail(userRequest.getEmail());

    return user;
  }

  private Mono<User> setUserToCache(User user) {
    return template.opsForValue()
                   .set(String.valueOf(user.getId()), user)
                   .map(result -> {
                     if (!result) {
                       throw new RedisCommandExecutionException("redis set command error");
                     }

                     return user;
                   });
  }
}
