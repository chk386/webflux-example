package com.nhn.reactor;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

/**
 * @author haekyu cho
 */

@ExtendWith(OutputCaptureExtension.class)
class A05_ColdVsHotTest {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Test
  @DisplayName("flux: cold 테스트")
  void coldTest(CapturedOutput output) throws InterruptedException {
    final var source = Flux.fromIterable(List.of("blue", "green", "orange", "purple"))
                           .map(String::toUpperCase);

    source.subscribe(color1 -> logger.info("Subscriber 1: {}", color1));
    Thread.sleep(3000);
    source.subscribe(color2 -> logger.info("Subscriber 2: {}", color2));

    assertThat("2번 실행된다.",
               extractColorsFromConsole(output),
               contains("BLUE", "GREEN", "ORANGE", "PURPLE", "BLUE", "GREEN", "ORANGE", "PURPLE"));
  }

  @Test
  @DisplayName("flux: hot 테스트")
  void hotTest(CapturedOutput output) {
    DirectProcessor<String> hotSource = DirectProcessor.create();
    Flux<String> hotFlux = hotSource.map(String::toUpperCase);

    hotSource.onNext("black");
    hotSource.onNext("red");
    hotFlux.subscribe(d -> logger.info("Subscriber 1 to Hot Source: {}", d));

    hotSource.onNext("blue");
    hotSource.onNext("green");

    hotFlux.subscribe(d -> logger.info("Subscriber 2 to Hot Source: {}", d));

    hotSource.onNext("orange");
    hotSource.onNext("purple");
    hotSource.onComplete();

    assertThat("구독을 하게 되면 실행된다.",
               extractColorsFromConsole(output),
               contains("BLUE", "GREEN", "ORANGE", "ORANGE", "PURPLE", "PURPLE"));
  }

  @Test
  @DisplayName("flux: integer")
  void hotConnectableFlux() throws Exception {
    ConnectableFlux<Integer> sink = Flux.<Integer>create(fluxSink -> {
      var i = 0;
      while (true) {
        fluxSink.next(i);
        i++;
        try {
          Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
      }
    }).take(10)
      .log()
      .subscribeOn(Schedulers.newSingle("HOT FLUX"))
      .publish();

    sink.connect();

    Thread.sleep(2500);
    final Flux<Integer> newFlux = Flux.range(1000, 10)
                                      .delayElements(Duration.ofMillis(500))
                                      .publishOn(Schedulers.newSingle("COLD FLUX"))
                                      .log();

    final Flux<Integer> merge = Flux.merge(sink, newFlux)
                                    .subscribeOn(Schedulers.newSingle("MERGE FLUX"));

    StepVerifier.create(merge)
                .recordWith(ArrayList::new)
                .expectNextCount(17)
                .consumeRecordedWith(v -> logger.info("총 카운트 : {}", v.size()))
                .verifyComplete();
  }

  @NotNull
  private List<String> extractColorsFromConsole(CapturedOutput output) {
    return Arrays.stream(output.getOut()
                               .split("\n"))
                 .filter(v -> v.contains(": "))
                 .filter(v -> v.contains("ColdVsHotTest"))
                 .map(v -> v.substring(v.lastIndexOf(":") + 2))
                 .collect(toList());
  }
}
