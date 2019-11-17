package com.nhn.webflux.reactive.user;

import com.nhn.mongo.UserMongoReactiveRepository;
import com.nhn.webflux.reactive.user.entity.User;

import org.hamcrest.beans.HasPropertyWithValue;
import org.hamcrest.core.Every;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;

import java.time.Duration;
import java.util.ArrayList;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.iterableWithSize;

/**
 * @author haekyu cho
 */
@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserMongoReactiveRepositoryTest {

  private Logger logger = LoggerFactory.getLogger(this.getClass());
  private final UserMongoReactiveRepository userMongoReactiveRepository;

  public UserMongoReactiveRepositoryTest(UserMongoReactiveRepository userMongoReactiveRepository) {
    this.userMongoReactiveRepository = userMongoReactiveRepository;
  }

  @Test
  @Order(1)
  @DisplayName("mongoDB 등록 테스트")
  void createUserTest() {
    var input = Flux.range(19800827, 100)
                    .delayElements(Duration.ofMillis(100))
                    .map(index -> {
                      User user = new User();
                      user.setId(Integer.toUnsignedLong(index));
                      user.setName("haekyu.cho");
                      user.setEmail("haekyu.cho@nhn.com");

                      return user;
                    });

    var output = userMongoReactiveRepository.saveAll(input);

    StepVerifier.create(output)
                .consumeNextWith(user -> {
                  logger.info("saved user : {}", user.toString());
                  assertThat("19800827부터 시작", user.getId(), equalTo(19800827L));
                })
                .consumeNextWith(user -> {
                  logger.info("saved user : {}", user.toString());
                  assertThat("다음은 19800828", user.getId(), equalTo(19800828L));
                })
                .expectNextCount(98)
                .verifyComplete();

  }

  @Test
  @Order(2)
  @DisplayName("mongoDB 조회 테스트")
  void fetchUserTest() {
    var users = userMongoReactiveRepository.findAllById(Flux.range(19800827, 100)
                                                            .map(Integer::longValue));

    StepVerifier.create(users)
                .recordWith(ArrayList::new)
                .expectNextCount(100)
                .consumeRecordedWith(results -> {
                  assertThat("총 100개를 조회해야한다.", results, iterableWithSize(100));
                  assertThat("19800826보다 ID가 커야한다.",
                             results,
                             Every.everyItem(HasPropertyWithValue.hasProperty("id", greaterThan(19800826L))));
                })
                .verifyComplete();
  }
}