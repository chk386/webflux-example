package com.nhn.webflux.reactive.user;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.HandlerFunction;
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
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * <a href="https://www.youtube.com/watch?v=M3jNn3HMeWg">참고하세욤!</a>
 *
 * @author haekyu cho
 */

@Configuration
public class UserRouter {

    @Bean
    public RouterFunction<ServerResponse> userRoute() {
        PersonHandler handler = new PersonHandler();

        return route().nest(path("/user"),
                            builder -> builder.GET("/{id}", handler::getPerson)
                                              .POST("/", accept(APPLICATION_JSON), handler::createPerson)
                                              .PATCH("/",
                                                     accept(APPLICATION_JSON),
                                                     request -> ServerResponse.noContent().build()))
                      .GET("/stream", handler::stream)
                      .build();
    }

    public static class PersonHandler {
        // ...

        public Mono<ServerResponse> listPeople(ServerRequest request) {
            String id = request.pathVariable("id");

            return ServerResponse.ok().contentType(TEXT_PLAIN).bodyValue(id);
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

            Flux<String> flux2 = Flux.range(1, 10).delayElements(Duration.ofMillis(500)).map(v -> "B : " + v);

            Flux<String> merged = Flux.merge(flux1, flux2);

            return ServerResponse.ok().contentType(TEXT_EVENT_STREAM).body(flux1.log(), List.class);
        }

        public Mono<ServerResponse> getPerson(ServerRequest request) {
            String id = request.pathVariable("id");

            return ServerResponse.ok().contentType(TEXT_PLAIN).bodyValue(id);
        }
    }
}
