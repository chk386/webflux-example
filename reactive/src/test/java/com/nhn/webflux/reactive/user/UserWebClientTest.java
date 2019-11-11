package com.nhn.webflux.reactive.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestConstructor;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebInputException;

import java.time.Duration;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

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

    private final long id = 10;
    private final String name = "harden";
    private final String baseUrl = "http://localhost:8080";

    @Test
    @DisplayName("webclient : create, retrieve를 이용한 webclient 테스트")
    void getUserByWebClient_create_retrieve() {
        StepVerifier.create(userWebClient.getUserByRetrieve(baseUrl, id, name))
                    .thenConsumeWhile(user -> name.concat("@nhn.com")
                                                  .equals(user.getEmail()))
                    .verifyComplete();
    }

    @Test
    @DisplayName("webclient : create, retrieve를 이용한 webclient 테스트2")
    void getUserByWebClient_create_retrieve2() {
        StepVerifier.create(userWebClient.getUserByRetrieve("http://localhost:9999", id, name))
                    .verifyError(ServerWebInputException.class);
    }

    @Test
    @DisplayName("webclient : builder, exchange를 이용한 webclient 테스트")
    void getUserByWebClient_builder_exchange() {
        StepVerifier.create(userWebClient.getUserByExchange(baseUrl, id, name))
                    .thenConsumeWhile(user -> name.concat("@nhn.com")
                                                  .equals(user.getEmail()))
                    .verifyComplete();
    }

    @Test
    @DisplayName("webclient : 유저 등록")
    void createUserTest() {
        StepVerifier.create(userWebClient.createUserByRetrieve(baseUrl, "davis"))
                    .thenConsumeWhile(user -> user.getId() == 9999)
                    .verifyComplete();
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    void bulkUsers() {
        ClassPathResource resource = new ClassPathResource("sample.txt");
        Mono<String> stringMono = WebClient.create(baseUrl)
                                           .post()
                                           .uri("/users/bulk")
                                           .contentType(MULTIPART_FORM_DATA)
                                           .header("clientId", "webflux")
                                           .body(BodyInserters.fromMultipartData("files", resource))
                                           .retrieve()
                                           .bodyToMono(String.class)
                                           .log();

        StepVerifier.create(stringMono)
                    .thenAwait(Duration.ofHours(1))
                    .expectComplete()
                    .verify(Duration.ZERO);

    }
}