package com.nhn.reactor;

import com.sun.jdi.VMOutOfMemoryException;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

class A04_BackpressureTest {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private Subscription subscription;

  @Test
  void backPressureBaseTest() throws InterruptedException {
    var countDownLatch = new CountDownLatch(10);

    Flux.range(1, 10)
        .log()
        .delayElements(Duration.ofMillis(100))
        .publishOn(Schedulers.newSingle("DELAY"))
        .log()
        .publishOn(Schedulers.newSingle("SUBSCRIBER"))
        .log()
        .subscribe(new Subscriber<>() {

          Subscription subscription;

          @Override
          public void onSubscribe(Subscription s) {
            this.subscription = s;
            this.subscription.request(100);
          }

          @Override
          public void onNext(Integer integer) {
            countDownLatch.countDown();
            final int threadActiveCount = Thread.activeCount();

            logger.info("integer: {},  active thread count : {}", integer, threadActiveCount);

            if(threadActiveCount < 10) {
              this.subscription.request(1);
            }else {
              this.subscription.cancel();
            }

            assertThat("쓰레드 10개 이하일 경우에만 실행된다.", threadActiveCount, lessThanOrEqualTo(10));
          }

          @Override
          public void onError(Throwable t) {
          }

          @Override
          public void onComplete() {
          }
        });

    countDownLatch.await(1500, TimeUnit.of(ChronoUnit.MILLIS));
  }

  @Test
  void backpressureTest() {
    BoardRepository boardRepository = new BoardRepository();

    var freeMemory = new CountDownLatch(100);

    var publisher = boardRepository.findAll()
                                   .log()
                                   .publishOn(Schedulers.newSingle("SUBSCRIBER"))
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
                                     } else if (freeMem % 10 == 0) {
                                       this.subscription.request(10);
                                     }
                                   })
                                   .doOnCancel(() -> logger.warn("cancel"))
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