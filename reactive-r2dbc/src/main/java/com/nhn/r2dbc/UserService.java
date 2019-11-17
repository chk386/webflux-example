package com.nhn.r2dbc;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Mono;

/**
 * @author haekyu cho
 */

@Service
public class UserService {
  private final UserReactiveRepository userReactiveRepository;

  public UserService(UserReactiveRepository userReactiveRepository) {
    this.userReactiveRepository = userReactiveRepository;
  }

  @Transactional
  public Mono<User> createUser(User user) {
    return userReactiveRepository.save(user);
  }
}
