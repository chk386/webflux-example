package com.nhn.webflux.reactive.user.service;

import com.nhn.webflux.reactive.team.repository.TeamRepository;
import com.nhn.webflux.reactive.user.entity.User;
import com.nhn.webflux.reactive.user.model.UserRequest;
import com.nhn.webflux.reactive.user.model.UserResponse;
import com.nhn.webflux.reactive.user.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Function;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class UserService {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final UserRepository userRepository;
  private final TeamRepository teamRepository;

  public UserService(UserRepository userRepository, TeamRepository teamRepository) {
    this.userRepository = userRepository;
    this.teamRepository = teamRepository;
  }

  public Mono<UserResponse> getUser(long id) {
    return Mono.fromCallable(() -> userRepository.findById(id)
                                                 .map(getUserUserResponseFunction())
                                                 .orElseThrow(Exception::new))
               .subscribeOn(Schedulers.boundedElastic());
  }

  @Transactional
  public Mono<User> save(UserRequest userRequest) {
    return Mono.fromCallable(() -> userRepository.saveAndFlush(toUser(userRequest)))
               .doOnError(e -> logger.error("DB 저장 실패", e))
               .subscribeOn(Schedulers.boundedElastic());
  }

  private User toUser(UserRequest userRequest) {
    User user = new User();
    user.setId(userRequest.getId());
    user.setName(userRequest.getName());
    user.setEmail(userRequest.getEmail());
    user.setTeam(teamRepository.getOne(userRequest.getTeamId()));

    return user;
  }

  private Function<User, UserResponse> getUserUserResponseFunction() {
    return user -> new UserResponse(user.getId(),
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
