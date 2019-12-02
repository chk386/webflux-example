package com.nhn.reactor;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;

class A02_MonoTest {

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  @Test
  @DisplayName("mono를 처음 만들어보고 map을 이용하여 emit된 문자열을 숫자로 바꾼다.")
  void monoBasicTest() {
    final String num1 = "1";

    Mono.just(num1)
        .log()
        .subscribeOn(Schedulers.newSingle("mono"))
        .doOnNext(v -> logger.info("data type is {}", v.getClass()))
        .map(Integer::parseInt)
        .doOnNext(v -> logger.info("data type is {}", v.getClass()))
        .doOnSubscribe(s -> logger.info("최초 한번 실행"))
        .subscribe(v -> assertThat("숫자 1로 변환", v, equalTo(1)), e -> {}, () -> {
          logger.info("완료");
        });
  }

  @Test
  @DisplayName("subscriber가 데이터를 처리중 에러 발생시 테스트")
  void monoError1() {
    var notNum = "A";

    Mono.just(notNum)
        .log()
        .map(Integer::parseInt)
        .doOnError(e -> logger.error("occur error"))
        .subscribe(v -> {}, e -> assertThat("A는 숫자가 아니다.", notNum, not(instanceOf(Integer.class))));
  }

  @Test
  @DisplayName("subscriber가 데이터를 처리중 에러 발생시 테스트")
  void monoError2() {
    Mono.error(NumberFormatException::new)
        .log()
        .doOnError(e -> assertThat("A는 숫자가 아니다.", e, Matchers.instanceOf(NumberFormatException.class)))
        .subscribe();
  }

  @Test
  @DisplayName("멀티 라인 문자열을 한 라인씩 1000ms으로 subscriber에게 푸시를 하여 테스트를 진행한다.")
  void monoDelay() {
    var fluxFromMono = Mono.just("hello\nwebflux")
                           .log()
                           .flatMapMany(s -> Flux.fromStream(Arrays.stream(s.split("\n")))
                                                 .log()
                                                 .delayElements(Duration.ofMillis(1000)));

    StepVerifier.create(fluxFromMono)
                .expectNext("hello")
                .expectNext("webflux")
                .verifyComplete();
  }

  @Test
  @DisplayName("mono first, zip, zipWith 테스트")
  void monoFirst() {
    var mono1 = Mono.just("1")
                    .delayElement(Duration.ofSeconds(3));
    var mono2 = Mono.just("2")
                    .delayElement(Duration.ofSeconds(1));
    var mono3 = Mono.just("3")
                    .delayElement(Duration.ofSeconds(2));

    var first = Mono.first(mono1, mono2, mono3)
                    .log();

    StepVerifier.create(first)
                .expectNext("2")
                .verifyComplete();

    final long start = System.currentTimeMillis();

    StepVerifier.create(Mono.zip(mono1, mono2, mono3))
                .consumeNextWith(tuple3 -> {
                  assertEquals("1", tuple3.getT1());
                  assertEquals("2", tuple3.getT2());
                  assertEquals("3", tuple3.getT3());

                  long time = System.currentTimeMillis() - start;
                  MatcherAssert.assertThat("3개의 mono.zip의 실행시간 약 3000ms이다.",
                                           time,
                                           allOf(greaterThan(3000L), lessThan(4000L)));
                })
                .verifyComplete();

    var zipWith = Mono.just("A")
                      .zipWith(Mono.just(1), (s, integer) -> s + " : " + integer.toString());

    StepVerifier.create(zipWith)
                .expectNext("A : 1")
                .verifyComplete();
  }
}
