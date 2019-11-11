package com.nhn.webflux.reactive.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.web.server.ServerWebInputException;

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
}