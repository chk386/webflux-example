package com.nhn.webflux.reactive.user.handler;

import com.nhn.webflux.reactive.team.repository.TeamRepository;
import com.nhn.webflux.reactive.user.entity.User;
import com.nhn.webflux.reactive.user.model.UserRequest;
import com.nhn.webflux.reactive.user.model.UserResponse;
import com.nhn.webflux.reactive.user.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;

import java.util.List;

import javax.transaction.Transactional;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * <a href="https://projectreactor.io/docs/core/release/reference/#faq.wrap-blocking">wrap blocking</a>
 */
@Component
public class UserHandlerBlocking {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final UserRepository userRepository;
  private final TeamRepository teamRepository;

  public UserHandlerBlocking(UserRepository userRepository, TeamRepository teamRepository) {
    this.userRepository = userRepository;
    this.teamRepository = teamRepository;
  }

  public Mono<ServerResponse> getUser(ServerRequest request) {
    final long id = Long.parseLong(request.pathVariable("id"));

    return ServerResponse.ok()
                         .body(Mono.fromCallable(() -> userRepository.findById(id)
                                                                     .map(UserHandlerBlocking::toUserResponse))
                                   .subscribeOn(Schedulers.boundedElastic()), UserResponse.class);

  }

  public Mono<ServerResponse> createUser(ServerRequest request) {
    return request.bodyToMono(UserRequest.class)
                  .doOnNext(userRequest -> {
                    if (userRequest.getId() != 0) {
                      throw new ServerWebInputException("id는 0이여야 합니다.");
                    }
                  })
                  .flatMap(userRequest -> { // Mono<UserRequest> -> Mono<ServerResponse>
                    final Mono<List<User>> user = Mono.fromCallable(() -> List.of(save(userRequest), save(userRequest)))
                                                      .doOnError(e -> logger.error("DB 저장 실패", e))
                                                      .subscribeOn(Schedulers.boundedElastic());

                    return ServerResponse.ok()
                                         .body(user, List.class);
                  });
  }

  @Transactional
  public User save(UserRequest userRequest) {
    return userRepository.saveAndFlush(toUser(userRequest));
  }

  private User toUser(UserRequest userRequest) {
    User user = new User();
    user.setId(userRequest.getId());
    user.setName(userRequest.getName());
    user.setEmail(userRequest.getEmail());
    user.setTeam(teamRepository.getOne(userRequest.getTeamId()));

    return user;
  }

  static UserResponse toUserResponse(User user) {
    return new UserResponse(user.getId(),
                            user.getName(),
                            user.getEmail(),
                            user.getTeam()
                                .getId(),
                            user.getTeam()
                                .getName(),
                            user.getTeam()
                                .getCompany()
                                .getId(),
                            user.getTeam()
                                .getCompany()
                                .getName());
  }
}
