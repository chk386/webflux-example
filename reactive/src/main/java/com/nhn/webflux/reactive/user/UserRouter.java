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
import java.util.function.Function;

import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

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
//                  .before(request -> request)
                  .filter(clientFilterFunction())
//                  .after((request, response) -> response)
                  .build();
  }

  private HandlerFilterFunction<ServerResponse, ServerResponse> clientFilterFunction() {
    return (request, next) -> {
      checkClientId(request);

      return next.handle(request)
        .flatMap(produce(request));
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
                                  .doOnNext(v -> log.info("[kafka producer] topic : [webflux], value : {}", requestLog))
                                  .map(v -> response);
    };
  }

  private void checkClientId(ServerRequest request) {
    boolean present = request.headers()
                             .header("clientId")
                             .stream()
                             .findAny()
                             .isPresent();

    if (!present) {
      log.warn("헤더 clientId를 넣어주세요.");
    }
  }
}
