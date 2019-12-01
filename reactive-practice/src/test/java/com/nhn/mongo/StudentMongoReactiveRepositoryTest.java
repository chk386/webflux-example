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

/**
 * @author haekyu cho
 */
@DataMongoTest
@ContextConfiguration(classes = {MongoConfiguration.class})
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StudentMongoReactiveRepositoryTest {

  @Test
  void createStudentTest() {

  }
  @Test
  void getStudentTest() {

  }
}