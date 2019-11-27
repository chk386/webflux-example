package com.nhn.reactor;

import com.sun.jdi.VMOutOfMemoryException;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.MemoryUsage;
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
                                       logger.warn("메모리 부족 -> pusblisher에게 cancel");
                                       throw new VMOutOfMemoryException("메모리가 부족합니다.");
                                     }

                                     if (freeMem <= 30 && freeMem > 1) {
                                       logger.info("남은 메모리 용량 : {}%, 1개씩 전송해주세요", freeMem);
                                       this.subscription.request(1);
                                     }

                                     if(freeMem%10 == 0) {
                                       this.subscription.request(10);
                                     }
                                   })
                                   .doOnCancel(() -> logger.warn("cancel요청"))
                                   .doOnSubscribe(s -> this.subscription = s);


    StepVerifier.create(publisher)
                .expectNextCount(98)
                .verifyError(VMOutOfMemoryException.class);

  }

  static class BoardRepository {

    Flux<Long> findAll() {
      return Flux.interval(Duration.ofMillis(100));
    }
  }
}