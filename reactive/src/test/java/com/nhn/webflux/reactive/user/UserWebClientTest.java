package com.nhn.webflux.reactive.user;

import com.nhn.webflux.configuration.WebClientConfiguration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author haekyu cho
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class UserWebClientTest {

    private final UserWebClient userWebClient;

    public UserWebClientTest(UserWebClient userWebClient) {
        this.userWebClient = userWebClient;
    }

    @Test
    @DisplayName("webclient조회 테스트")
    void getUserByWebClient() {
        final long id = 10;
        final String name = "harden";
        final Mono<User> userMono = userWebClient.getUser(id, name);

        StepVerifier.create(userMono)
                    .thenConsumeWhile(user -> name.concat("@nhn.com")
                                                  .equals(user.getEmail()))
                    .verifyComplete();

        //        WebTestClient.bindToServer()
        //                     .baseUrl("http://localhost:8080")
        //                     .build()
        //                     .get()
        //                     .uri("/usesrs/{id}?name={name}", 10, "harden")
        //                     .exchange()
        //                     .expectStatus()
        //                     .is2xxSuccessful()
        //                     .expectHeader()
        //                     .contentType(APPLICATION_JSON)
        //                     .expectBody(User.class)
        //                     .consumeWith(result -> {
        //                         final User user = result.getResponseBody();
        //                         assert user != null;
        //                         assertThat("harden의 이름은 harden@nhn.com이여야 한다.",
        //                                    user.getEmail(),
        //                                    Matchers.equalTo("harden@nhn.com"));
        //                     });

    }

}