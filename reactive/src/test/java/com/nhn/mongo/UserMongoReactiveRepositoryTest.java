package com.nhn.mongo;

import com.nhn.webflux.configuration.MongoConfiguration;
import com.nhn.webflux.reactive.user.entity.User;

import org.hamcrest.beans.HasPropertyWithValue;
import org.hamcrest.core.Every;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.reactivestreams.Subscription;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestConstructor;

import java.time.Duration;
import java.util.ArrayList;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.iterableWithSize;

@DataMongoTest
@ContextConfiguration(classes = {MongoConfiguration.class})
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserMongoReactiveRepositoryTest {

  private final UserMongoReactiveRepository userMongoReactiveRepository;

  public UserMongoReactiveRepositoryTest(UserMongoReactiveRepository userMongoReactiveRepository) {
    this.userMongoReactiveRepository = userMongoReactiveRepository;
  }

  @Test
  @Order(1)
  @DisplayName("mongoDB 등록 테스트")
  void createUserTest() {
    var input = Flux.range(800827, 100)
                    .map(index -> {
                      User user = new User();
                      user.setId(Integer.toUnsignedLong(index));
                      user.setName("haekyu.cho");
                      user.setEmail("haekyu.cho@nhn.com");

                      return user;
                    });

    StepVerifier.create(userMongoReactiveRepository.saveAll(input))
                .expectNextCount(100)
                .verifyComplete();
  }

  private Subscription subscription;

  @Test
  @Order(2)
  @DisplayName("mongoDB 조회 테스트")
  void fetchUserTest() {
    var users = userMongoReactiveRepository.findAllById(Flux.range(800827, 100)
                                                            .map(Integer::longValue))
                                           .publishOn(Schedulers.newSingle("MONGO"))
                                           .log()
                                           .doOnSubscribe(v -> {
                                             this.subscription = v;
                                           })
                                           .doOnNext(v -> {
                                             if (v.getId() > 800840L) {
                                               this.subscription.request(1);
                                             }
                                           });

    StepVerifier.create(users)
                .recordWith(ArrayList::new)
                .expectNextCount(100)
                .consumeRecordedWith(results -> {
                  assertThat("총 100개를 조회해야한다.", results, iterableWithSize(100));
                  assertThat("800827보다 같거나 크다.",
                             results,
                             Every.everyItem(HasPropertyWithValue.hasProperty("id", greaterThanOrEqualTo(800827L))));
                })
                .verifyComplete();
  }
}