package com.nhn.r2dbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

import reactor.core.publisher.Mono;

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
      final Mono<User> user1 = userReactiveRepository.findById(22L)
                                                     .delayElement(Duration.ofSeconds(2));
      final Mono<User> user2 = userService.createUser(new User("james harden", "harden@nhn.com"))
                                          .delayElement(Duration.ofSeconds(3));

      final long start = System.currentTimeMillis();
      CountDownLatch latch = new CountDownLatch(1);

      Mono.zip(user1, user2)
          .subscribe(tuple -> {
            final User t1 = tuple.getT1();
            final User t2 = tuple.getT2();

            logger.info("조회 : {}", t1.toString());
            logger.info("등록 : {}", t2.toString());
            logger.info("실행시간 : {} ms", System.currentTimeMillis() - start);

            latch.countDown();
          });

      latch.await();
    };
  }
}
