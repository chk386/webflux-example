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

/**
 * @author haekyu cho
 */

@Component
public class UserHandlerRedis {

  private final ReactiveRedisTemplate<String, User> template;
  private final UserRepository userRepository;

  public UserHandlerRedis(ReactiveRedisTemplate<String, User> template, UserRepository userRepository) {
    this.template = template;
    this.userRepository = userRepository;
  }

  public Mono<ServerResponse> createUser(ServerRequest request) {
    // todo: UserRequest를 body로 받아서 User로 변환한 후 cache에 저장, response body는 user를 반환
    return null;
  }

  public Mono<ServerResponse> getUser(ServerRequest request) {
    // todo : id가 캐쉬에 있으면 바로 리턴, 없으면 db에서 조회 후 캐쉬에 저장return ServerResponse.ok()
     return null;
  }

  private User toUser(UserRequest userRequest) {
    User user = new User();
    user.setId(userRequest.getId());
    user.setName(userRequest.getName());
    user.setEmail(userRequest.getEmail());

    return user;
  }
}
