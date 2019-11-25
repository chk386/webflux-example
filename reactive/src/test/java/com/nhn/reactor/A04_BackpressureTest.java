package com.nhn.reactor;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class A04_BackpressureTest {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private Subscription subscription;

  @Test
  void backpressureTest() {
    BoardRepository boardRepository = new BoardRepository();

    var freeMemory = new CountDownLatch(100);

    var publisher = boardRepository.findAll()
                                   .log()
                                   .doOnNext(v -> {
                                     freeMemory.countDown();
                                     var freeMem = freeMemory.getCount();

                                     if (freeMem == 1) {
                                       this.subscription.cancel();
                                     }

                                     if (freeMem <= 30 && freeMem > 1) {
                                       logger.info("남은 메모리 용량 : {}%, 1개씩 전송해주세요", freeMem);
                                       this.subscription.request(1);
                                     }
                                   })
                                   .doOnCancel(() -> {
                                     logger.warn("메모리가 1%남았기 때문에 pusblisher에게 cancel");
                                     freeMemory.countDown();
                                   })
                                   .doOnSubscribe(s -> {
                                     this.subscription = s;
                                   });

    StepVerifier.create(publisher)
                .expectNextCount(99)
                .expectComplete()
                .verify();

  }

  static class BoardRepository {

    Flux<Long> findAll() {
      return Flux.interval(Duration.ofMillis(100));
    }
  }
}