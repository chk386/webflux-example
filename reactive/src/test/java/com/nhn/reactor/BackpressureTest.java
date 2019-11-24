package com.nhn.reactor;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import reactor.core.publisher.Flux;

public class BackpressureTest {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private Subscription subscription;

  @Test
  void backpressureTest() throws InterruptedException {
    BoardRepository boardRepository = new BoardRepository();

    var freeMemory = new CountDownLatch(100);

    boardRepository.findAll()
                   .log()
                   .doOnNext(v -> {
                     freeMemory.countDown();
                     var freeMem = freeMemory.getCount();

                     if (freeMem == 1) {
                       this.subscription.cancel();
                     }

                     if (freeMem % 10 == 0 && freeMem > 30) {
                       this.subscription.request(10);
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
                     this.subscription.request(10);
                   })
                   .subscribe();

    freeMemory.await(60, TimeUnit.SECONDS);
  }

  static class BoardRepository  {

    Flux<Long> findAll() {
      return Flux.interval(Duration.ofMillis(300));
    }
  }
}