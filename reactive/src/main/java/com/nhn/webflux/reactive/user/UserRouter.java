package com.nhn.webflux.reactive.user;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.time.Duration;
import java.util.List;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * <a href="https://www.youtube.com/watch?v=M3jNn3HMeWg">참고하세욤!</a>
 * https://shortstories.gitbook.io/studybook/spring-webflux
 * @author haekyu cho
 */
//RouterFunction 을 여러 개 등록하면 가장 먼저 일치하는 HandlerFunction 을 실행하므로 .path("/**") ,
// .path("/somePath")처럼 중첩된 범위를 가지는 RouterFunction 을 여러 개 등록한다면 반드시 좀 더 자세한 범위의 RouterFunction 을 먼저 등록해야 함.
// /** 다음에 /somePath를 등록하게 되면 /somePath에 등록한 HandlerFunction 은 절대로 호출되지 않음.
@Configuration
public class UserRouter {

    private final UserHandler userHandler;

    public UserRouter(UserHandler userHandler) {
        this.userHandler = userHandler;
    }

    @Bean
    public RouterFunction<ServerResponse> userRoute() {
        return route().nest(path("/users"),
                            nested -> nested.GET("/{id}", userHandler::getUser)
                                            .POST("/", contentType(APPLICATION_JSON), userHandler::createUser)
            //                                            .POST("/", accept(APPLICATION_JSON), handler::createPerson)
                            //                                            .DELETE("/{id}",
                            //                                                    headers(v -> v.header("appKey")
                            //                                                                  .contains("webflux")),
                            //                                                    userHandler::getUser)
        )
                      .build();
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
