package com.nhn.rdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

import reactor.core.publisher.Mono;

import static java.time.Duration.ofSeconds;

/**
 * <a href="https://docs.spring.io/spring-data/r2dbc/docs/1.0.0.BUILD-SNAPSHOT/reference/html/#r2dbc.repositories">r2jdbc repositories reference</a>>
 */

@SpringBootApplication
public class R2dbcApplication {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final UserService userService;

  public R2dbcApplication(UserService userService) {
    this.userService = userService;
  }

  public static void main(String[] args) {
    SpringApplication.run(R2dbcApplication.class);
  }

  @Bean
  ApplicationRunner run(UserReactiveRepository userReactiveRepository) {
    return args -> {
      var user1 = userReactiveRepository.findById(22L)
                                        .delayElement(ofSeconds(1));
      var user2 = userService.createUser(new User("james harden", "harden@nhn.com"))
                             .delayElement(ofSeconds(3));
      var user3 = Mono.just("hello webflux").delayElement(ofSeconds(5));

      var start = System.currentTimeMillis();
      var latch = new CountDownLatch(1);

      Mono.zip(user1, user2, user3)
          .subscribe(tuple -> {
            logger.info("user1 : {}", tuple.getT1().toString());
            logger.info("user2 : {}", tuple.getT2().toString());
            logger.info("user3 : {}", tuple.getT3());
            logger.info("실행시간 : {} ms", System.currentTimeMillis() - start);

            latch.countDown();
          });

      latch.await();
    };
  }
}
