package com.nhn.webflux.reactive.user.webclient;

import com.nhn.webflux.reactive.user.UserWebClient;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestConstructor;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebInputException;

import java.util.ArrayList;
import java.util.Objects;

import reactor.test.StepVerifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

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
    StepVerifier.create(userWebClient.getUserByRetrieve(baseUrl + "/wrong/", id, name))
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

  @Test
  @DisplayName("multipart 파일 업로드 테스트")
  void bulkUsers() {
    ClassPathResource resource = new ClassPathResource("sample.txt");
    var flux = WebClient.create(baseUrl)
                        .post()
                        .uri("/users/bulk")
                        .contentType(MULTIPART_FORM_DATA)
                        .header("clientId", "webflux")
                        .body(BodyInserters.fromMultipartData(Objects.requireNonNull(resource.getFilename()), resource))
                        .retrieve()
                        .bodyToFlux(String.class);

    StepVerifier.create(flux)
                .recordWith(ArrayList::new)
                .thenConsumeWhile(v -> !v.isEmpty())
                .consumeRecordedWith(lines -> {
                  var size = lines.size();
                  var last = lines.stream()
                                  .skip(size - 1)
                                  .findFirst()
                                  .orElseThrow(RuntimeException::new);

                  assertThat("sample.txt의 마지막 라인에는 END가 포함되어야 한다.", last, containsString("END"));
                  assertThat("sample.txt의 라인수는 176이다.", size, equalTo(176));
                })
                .verifyComplete();
  }
}