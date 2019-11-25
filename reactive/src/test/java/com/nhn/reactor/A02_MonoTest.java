package com.nhn.reactor;

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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;

/**
 * @author haekyu cho
 */

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
}
