package com.nhn.webflux.reactive.user;

import com.nhn.webflux.reactive.user.model.UserRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerErrorException;
import org.springframework.web.server.ServerWebInputException;

import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * <p>create(shortcut) <-> builder(filter, clientConnector)</p>
 * <p>retrieve(shortcut) <-> exchange</p>
 * <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html#webflux-client">webflux reference</a>
 *
 * @author haekyu.cho@nhnent.com
 */
@Component
public class UserWebClient {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final String clientId = "clientId";
  private final String webflux = "webflux";

  public Mono<UserRequest> getUserByRetrieve(String baseUrl, long id, String name) {
    return WebClient.create(baseUrl)
                    .get()
                    .uri("/users/{id}?name={name}", id, name)
                    .header(clientId, "webflux")
                    .accept(APPLICATION_JSON)
                    .retrieve()
                    .onStatus(HttpStatus::is5xxServerError, response -> {
                      logger.error("5xx 에러발생");
                      return Mono.error(new Exception());
                    })
                    .onStatus(HttpStatus::is4xxClientError, response -> {
                      logger.error("4xx 에러발생");
                      return Mono.error(new ServerWebInputException("클라이언트 호출 오류"));
                    })
                    .bodyToMono(UserRequest.class);
  }

  public Mono<UserRequest> getUserByExchange(String baseUrl, long id, String name) {
    return WebClient.builder()
                    .filter((request, next) -> next.exchange(ClientRequest.from(request)
                                                                          .header("foo", "bar")
                                                                          .build()))
                    .baseUrl(baseUrl)
                    .build()
                    .get()
                    .uri("/users/{id}?name={name}", id, name)
                    .header(clientId, webflux)
                    .accept(APPLICATION_JSON)
                    .exchange()
                    .flatMap(response -> {
                      response.headers()
                              .header(clientId)
                              .stream()
                              .findAny()
                              .ifPresent(logger::info);

                      HttpStatus httpStatus = response.statusCode();
                      logger.info("응답 코드 : {}", httpStatus.getReasonPhrase());

                      if (httpStatus == HttpStatus.OK || httpStatus == HttpStatus.CREATED
                          || httpStatus == HttpStatus.NO_CONTENT) {
                        return response.bodyToMono(UserRequest.class);
                      } else if (httpStatus == HttpStatus.BAD_REQUEST || httpStatus == HttpStatus.UNAUTHORIZED
                                 || httpStatus == HttpStatus.FORBIDDEN || httpStatus == HttpStatus.NOT_FOUND) {
                        return Mono.error(new ServerWebInputException("클라이언트 오류 발생"));
                      } else if (httpStatus == HttpStatus.SERVICE_UNAVAILABLE
                                 || httpStatus == HttpStatus.INTERNAL_SERVER_ERROR) {
                        return Mono.error(new ServerErrorException("오류 발생", new Exception()));
                      } else {
                        return Mono.error(new Exception("나머지 오류 발생"));
                      }
                    });
  }

  public Mono<UserRequest> createUserByRetrieve(String baseUrl, String name) {
    return WebClient.create(baseUrl)
                    .post()
                    .uri("/users")
                    .contentType(APPLICATION_JSON)
                    .header(clientId, webflux)
                    .bodyValue(new UserRequest(name, name.concat("@nhn.com")))
                    .retrieve()
                    .bodyToMono(UserRequest.class);

  }
}
