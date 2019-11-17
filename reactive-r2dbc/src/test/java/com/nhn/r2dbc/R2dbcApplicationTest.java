package com.nhn.r2dbc;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestConstructor;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author haekyu cho
 */
//@DataR2dbcTest
@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class R2dbcApplicationTest {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final UserReactiveRepository userRepository;
  private final UserService userService;

  public R2dbcApplicationTest(UserReactiveRepository userRepository, UserService userService) {
    this.userRepository = userRepository;
    this.userService = userService;
  }

  @Test
  void getUser() {
    Mono<User> mono = userRepository.findById(22L)
                                    .doOnNext(user -> logger.info(user.toString()));

    StepVerifier.create(mono)
                .consumeNextWith(user -> {
                  assertEquals(user.getId(), 22L);
                  assertEquals(user.getEmail(), "dodo4513@nhn.com");
                })
                .verifyComplete();
  }

  @Test
  @Rollback
  void createUser() {
    User user1 = new User("aa", "aa@nhn.com");

    final Mono<User> userMono = userService.createUser(user1);

    StepVerifier.create(userMono)
                .consumeNextWith(user -> {
                  logger.info("new user : {}", user.toString());
                  assertEquals(user.getName(), "aa");
                })
                .verifyComplete();

  }
}