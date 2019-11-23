package com.nhn.webflux.reactive.user;

import com.nhn.mongo.RequestLog;
import com.nhn.webflux.reactive.user.handler.UserHandler;
import com.nhn.webflux.reactive.user.handler.UserHandlerBlocking;
import com.nhn.webflux.reactive.user.handler.UserHandlerRedis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.function.Function;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * 시작전 : preference -> code style -> java -> Wrapping and braces -> chained method calls -> wrap always
 *
 * <a href="https://www.youtube.com/watch?v=M3jNn3HMeWg">참고하세욤!</a>
 * https://shortstories.gitbook.io/studybook/spring-webflux
 * 2가지 : RouterFunction (Webmvc requestMapping, filter) , HandlerFunction (request와 response 처리)
 * filter는 before, filter, after : similar functionality by using @ControllerAdvice, ServletFilter
 * route, nest, path, GET, RequestPredicate,  and, or, add(otherRoute)
 *
 * @author haekyu cho
 */
//RouterFunction 을 여러 개 등록하면 가장 먼저 일치하는 HandlerFunction 을 실행하므로 .path("/**") ,
// .path("/somePath")처럼 중첩된 범위를 가지는 RouterFunction 을 여러 개 등록한다면 반드시 좀 더 자세한 범위의 RouterFunction 을 먼저 등록해야 함.
// /** 다음에 /somePath를 등록하게 되면 /somePath에 등록한 HandlerFunction 은 절대로 호출되지 않음.
@Configuration
public class UserRouter {

  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private final UserHandler userHandler;
  private final UserHandlerBlocking userHandlerBlocking;
  private final UserHandlerRedis userHandlerRedis;
  private final ReactiveKafkaProducerTemplate<String, Object> kafkaProducerTemplate;

  public UserRouter(UserHandler userHandler, UserHandlerBlocking userHandlerBlocking, UserHandlerRedis userHandlerRedis,
                    ReactiveKafkaProducerTemplate<String, Object> kafkaProducerTemplate) {
    this.userHandler = userHandler;
    this.userHandlerBlocking = userHandlerBlocking;
    this.userHandlerRedis = userHandlerRedis;
    this.kafkaProducerTemplate = kafkaProducerTemplate;
  }

  @Bean
  public RouterFunction<ServerResponse> userRoute() {
    return route().path("/users",
                        b1 -> b1.GET("/{id}", userHandler::getUser)
                                .nest(contentType(APPLICATION_JSON),
                                      b2 -> b2.POST("/", userHandler::createUser)
                                              .PUT("/", userHandler::modifyUser))
                                .POST("/bulk", contentType(MULTIPART_FORM_DATA), userHandler::bulkUsers)
                                .GET("/blocking/{id}", userHandlerBlocking::getUser)
                                .POST("/blocking", contentType(APPLICATION_JSON), userHandlerBlocking::createUser))
                  .path("/users/redis",
                        b1 -> b1.GET("/{id}", userHandlerRedis::getUser)
                                .POST("/", userHandlerRedis::createUser))
                  .before(request -> request)
                  .filter(clientFilterFunction())
                  .after((request, response) -> response)
                  .build();
  }

  private HandlerFilterFunction<ServerResponse, ServerResponse> clientFilterFunction() {
    return (request, next) -> {
      checkClientId(request);

      var handle = next.handle(request);

      if (!kafkaProducerTemplate.getClass()
                                .getSimpleName()
                                .contains("mock")) {

        handle.flatMap(produce(request));
      }

      return handle;
    };
  }

  private Function<ServerResponse, Mono<? extends ServerResponse>> produce(ServerRequest request) {
    return response -> {
      RequestLog requestLog = new RequestLog(System.currentTimeMillis(),
                                             request.uri()
                                                    .toString(),
                                             request.remoteAddress()
                                                    .orElse(new InetSocketAddress(9999))
                                                    .getAddress()
                                                    .getHostAddress());

      return kafkaProducerTemplate.send("webflux", requestLog)
                                  .doOnNext(v -> log.info("[kafka producer] topic : [webflux], value : {}",
                                                          requestLog.toString()))
                                  .map(v -> response);
    };
  }

  private void checkClientId(ServerRequest request) {
    // 저희팀의 경우는 kotlin extensions를 이용하여
    boolean present = request.headers()
                             .header("clientId")
                             .stream()
                             .findAny()
                             .isPresent();

    if (!present) {
      log.warn("헤더 clientId를 넣어주세요.");
    }
  }

  public static class PersonHandler {
    // ...

    public Mono<ServerResponse> listPeople(ServerRequest request) {
      String id = request.pathVariable("id");

      return ServerResponse.ok()
                           .contentType(TEXT_PLAIN)
                           .bodyValue(id);
    }

    public Mono<ServerResponse> createPerson(ServerRequest request) {
      return null;
    }

    public Mono<ServerResponse> stream(ServerRequest request) {
      Flux<Integer> original = Flux.just(1, 2, 3, 4, 4, 4, 5, 6, 7, 8, 9, 10);

      Flux<String> flux1 = original.delayElements(Duration.ofMillis(500))
                                   //                                         .distinct()
                                   .groupBy(v -> v)
                                   .concatMap(Flux::count)
                                   .map(v -> "A : " + v);

      Flux<List<String>> bufferdFlux1 = flux1.bufferTimeout(3, Duration.ofMillis(1000));

      Flux<String> flux2 = Flux.range(1, 10)
                               .delayElements(Duration.ofMillis(500))
                               .map(v -> "B : " + v);

      Flux<String> merged = Flux.merge(flux1, flux2);

      return ServerResponse.ok()
                           .contentType(TEXT_EVENT_STREAM)
                           .body(flux1.log(), List.class);
    }

    public Mono<ServerResponse> getPerson(ServerRequest request) {
      String id = request.pathVariable("id");

      return ServerResponse.ok()
                           .contentType(TEXT_PLAIN)
                           .bodyValue(id);
    }
  }
}
