package com.nhn.webflux.reactive.user;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * @author haekyu.cho@nhnent.com
 */
@Component
public class UserWebClient {

    private final WebClient webClient;

    public UserWebClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<User> getUser(long id, String name) {
        return WebClient.create("http://localhost:8080")
                        .get()
                        .uri("/users/{id}?name={name}", id, name)
                        .header("clientId", "webflux")
                        .accept(APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(User.class);
    }
}
