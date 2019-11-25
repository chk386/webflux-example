package com.nhn.reactor;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;

import reactor.core.publisher.Flux;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.core.Is.is;

/**
 * @author haekyu cho
 */
@ExtendWith(OutputCaptureExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class A01_DualityTest {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final List<Integer> integers = List.of(1, 2, 3, 4, 5);

  @Test
  @Order(1)
  @DisplayName("Iterable 테스트")
  void iterableTest(CapturedOutput output) {
    for (Iterator<Integer> it = integers.iterator(); it.hasNext(); ) {
      Integer integer = it.next();

      logger.info("Iterable Pattern : {}", integer.toString());
    }

    assertThat("1,2,3,4,5가 출력되어야 한다.", captureOutput(output), everyItem(is(in(integers))));
  }

  @Test
  @Order(2)
  @DisplayName("Observable 테스트")
  @SuppressWarnings("deprecation")
  void observableTest(CapturedOutput output) {
    ExamObservable observable = new ExamObservable();
    observable.addObserver((o, arg) -> logger.info("Observable Pattern : {}", arg.toString()));

    observable.push(integers);

    assertThat("1,2,3,4,5가 출력되어야 한다.", captureOutput(output), everyItem(is(in(integers))));
  }

  @Test
  @Order(3)
  @DisplayName("Reactive Streams 테스트")
  void reactiveStreamsTest(CapturedOutput output) {
    Publisher<Integer> publisher = s -> integers.forEach(s::onNext);
    publisher.subscribe(new ExamSubscriber());

    assertThat("1,2,3,4,5가 출력되어야 한다.", captureOutput(output), everyItem(is(in(integers))));
  }

  @Test
  @Order(4)
  @DisplayName("Reactor 테스트")
  void reactorTest(CapturedOutput output) {
    Flux.fromIterable(integers)
        .subscribe(v -> {
          logger.info("Reactor : {}", v);
        });

    assertThat("1,2,3,4,5가 출력되어야 한다.", captureOutput(output), everyItem(is(in(integers))));
  }

  static class ExamObservable extends Observable {

    void push(List<Integer> integers) {
      integers.forEach(i -> {
        this.setChanged();
        this.notifyObservers(i);
      });
    }
  }

  public static class ExamSubscriber implements Subscriber<Integer> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void onSubscribe(Subscription s) {
      // 최초한번 실행
    }

    @Override
    public void onNext(Integer integer) {
      // publisher가 데이터를 emit할때 마다 실행
      logger.info("Reactive Streams : {}", integer);
    }

    @Override
    public void onError(Throwable t) {
    }

    @Override
    public void onComplete() {
    }
  }

  @NotNull
  private List<Integer> captureOutput(CapturedOutput output) {
    return Arrays.stream(output.getOut()
                               .split("\n"))
                 .filter(line -> line.contains("DualityTest"))
                 .map(line -> Integer.parseInt(line.substring(line.length() - 1)))
                 .collect(toList());
  }
}
