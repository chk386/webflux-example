package com.nhn.mongo;

import com.nhn.webflux.configuration.MongoConfiguration;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestConstructor;

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