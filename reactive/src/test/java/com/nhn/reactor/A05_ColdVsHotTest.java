package com.nhn.reactor;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.core.Is.is;

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

    var colors = Arrays.stream(output.getOut()
                                     .split("\n"))
                       .filter(v -> v.contains(": "))
                       .map(v -> v.substring(v.lastIndexOf(":") + 2))
                       .collect(toList());

    assertThat("2번 실행된다.",
               colors,
               containsInAnyOrder("BLUE", "GREEN", "ORANGE", "PURPLE", "BLUE", "GREEN", "ORANGE", "PURPLE"));

  }

  @Test
  @DisplayName("flux: hot 테스트")
  void hotTest() {
    DirectProcessor<String> hotSource = DirectProcessor.create();
    Flux<String> hotFlux = hotSource.map(String::toUpperCase);

    hotFlux.subscribe(d -> logger.info("Subscriber 1 to Hot Source: {}", d));

    hotSource.onNext("blue");
    hotSource.onNext("green");

    hotFlux.subscribe(d -> logger.info("Subscriber 2 to Hot Source: {}", d));

    hotSource.onNext("orange");
    hotSource.onNext("purple");
    hotSource.onComplete();
  }

  // https://hychul.github.io/development/2019/03/21/reactor-hot-stream/
  @Test
  void a() throws InterruptedException {
    Flux<Integer> source = Flux.range(1, 3)
                               .doOnSubscribe(s -> System.out.println("subscribed to source"));

    ConnectableFlux<Integer> co = source.publish();

    co.subscribe(System.out::println, e -> {}, () -> {});
    co.subscribe(System.out::println, e -> {}, () -> {});

    System.out.println("done subscribing");
    Thread.sleep(500);
    System.out.println("will now connect");

    co.connect();


    Flux<Integer> source2 = Flux.range(1, 3)
                               .doOnSubscribe(s -> System.out.println("subscribed to source2"));

    Flux<Integer> autoCo = source2.publish().autoConnect(2);

    autoCo.subscribe(System.out::println, e -> {}, () -> {});
    System.out.println("subscribed first");
    Thread.sleep(500);
    System.out.println("subscribing second");
    autoCo.subscribe(System.out::println, e -> {}, () -> {});
  }
}
