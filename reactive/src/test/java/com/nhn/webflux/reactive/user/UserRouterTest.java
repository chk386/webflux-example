package com.nhn.webflux.reactive.user;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder;
import com.nhn.webflux.reactive.team.entity.Team;
import com.nhn.webflux.reactive.team.repository.TeamRepository;
import com.nhn.webflux.reactive.user.entity.User;
import com.nhn.webflux.reactive.user.handler.UserHandler;
import com.nhn.webflux.reactive.user.handler.UserHandlerBlocking;
import com.nhn.webflux.reactive.user.handler.UserHandlerRedis;
import com.nhn.webflux.reactive.user.model.UserRequest;
import com.nhn.webflux.reactive.user.model.UserResponse;
import com.nhn.webflux.reactive.user.repository.UserRepository;
import com.nhn.webflux.reactive.user.service.UserService;

import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.time.Duration;

import reactor.core.publisher.Mono;

import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;

@ExtendWith({RestDocumentationExtension.class})
@WebFluxTest
@ContextConfiguration(classes = {UserRouter.class, UserHandler.class, UserHandlerBlocking.class})
@AutoConfigureRestDocs
class UserRouterTest {

  @MockBean
  UserHandlerRedis userHandlerRedis;

  @MockBean
  ReactiveRedisTemplate reactiveRedisTemplate;

  @MockBean
  ReactiveKafkaProducerTemplate<String, Object> reactiveKafkaProducerTemplate;

  @MockBean
  UserRepository userRepository;

  @MockBean
  TeamRepository teamRepository;

  @MockBean
  UserService userService;

  @Autowired
  private WebTestClient webTestClient;
  @Autowired
  private UserRouter userRouter;
  private ResourceSnippetParametersBuilder builder = ResourceSnippetParameters.builder();

  private static final String CLIENT_ID = "clientId";

  @Test
  @DisplayName("유저를 조회한다.")
  void getUser() {
    // @formatter:off
    webTestClient.get()
                 .uri("/users/{id}?name={name}", "1", "haekyu.cho")
                 .header(CLIENT_ID, "webflux")
                 .exchange()
                 .expectStatus().isOk()
                 .expectHeader().contentType(APPLICATION_JSON)
                 .expectBody(UserRequest.class)
                 .consumeWith(
                    document("get-user",
                      resource(builder.tag("[User]")
                                      .description("유저 조회한다.")
                                      .requestHeaders(headerWithName(CLIENT_ID).description("클라이언트 ID").optional())
                                      .pathParameters(parameterWithName("id").description("조회할 유저 ID"))
                                      .requestParameters(parameterWithName("name").description("조회할 유저 이름").optional())
                                      .responseHeaders(headerWithName(CONTENT_TYPE).description(APPLICATION_JSON_VALUE))
                                      .responseFields(userField())
                                      .build()))
                     );
    // @formatter:on
  }

  /**
   * <p> multipart는 아직 openapi3를 지원하지 않는다.</p>
   * <a href="https://github.com/ePages-de/restdocs-api-spec/issues/105">multipart not supported</a>
   */
  @Test
  @DisplayName("유저 대량 등록")
  void bulkUser() {
    Resource resource = new ClassPathResource("sample.txt");
    // @formatter:off
    webTestClient.mutate()
                 .responseTimeout(Duration.ofMinutes(5))
                 .build()
                 .post()
                 .uri("/users/bulk")
                 .accept(MULTIPART_FORM_DATA)
                 .header(CLIENT_ID, "webflux")
                 .body(BodyInserters.fromMultipartData("sample.txt", resource))
                 .exchange()
                 .expectStatus().isOk()
                 .expectHeader().valueEquals(CONTENT_TYPE, TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
                 .expectBody()
                 .consumeWith(
                   document("bulk-user",
                            requestParts(
                              partWithName(resource.getFilename()).description("The file to upload")
                            )
                   )
                 );
    // @formatter:on
  }

  @Test
  @DisplayName("유저를 등록한다.")
  void createUser() {
    UserRequest user = new UserRequest("derrick rose", "rose@nhn.com");
    // @formatter:off
    webTestClient.post()
                 .uri("/users")
                 .accept(APPLICATION_JSON)
                 .header(CLIENT_ID, "webflux")
                 .bodyValue(user)
                 .exchange()
                 .expectStatus().isCreated()
                 .expectHeader().contentType(APPLICATION_JSON)
                 .expectHeader().value(LOCATION, Matchers.containsString("users"))
                 .expectBody()
                 .consumeWith(document("create-user",
                                       resource(builder.tag("[User]")
                                                       .description("유저를 등록한다.")
                                                       .requestHeaders(headerWithName(CONTENT_TYPE).description(APPLICATION_JSON_VALUE),
                                                                       headerWithName(CLIENT_ID).description("클라이언트 ID").optional())
                                                       .requestFields(userField())
                                                       .responseHeaders(headerWithName(CONTENT_TYPE).description(APPLICATION_JSON_VALUE),
                                                                        headerWithName(LOCATION).description("회원 조회 uri"))
                                                       .responseFields(userField())
                                                       .build())));
    // @formatter:on
  }

  @Test
  @DisplayName("DB에서 유저를 가져온다.")
  void getUserBlockingTest() {
    var userResponse = new UserResponse(1, "jordan", "jordan@nhn.com", 1, "commerce", 1, "nhn");
    given(userService.getUser(anyLong())).willReturn(Mono.just(userResponse));

    // @formatter:off
    webTestClient.get()
                 .uri("/users/blocking/{id}", 25)
                 .accept(APPLICATION_JSON)
                 .header(CLIENT_ID, "webflux")
                 .exchange()
                 .expectStatus().isOk()
                 .expectHeader().contentType(APPLICATION_JSON)
                 .expectBody(UserResponse.class)
                 .consumeWith(document("get-user-blocking",
                                       resource(builder.tag("[User]")
                                                       .description("blocking 유저 조회")
                                                       .description("유저 조회한다.")
                                                       .requestHeaders(headerWithName(CLIENT_ID).description("클라이언트 ID").optional())
                                                       .pathParameters(parameterWithName("id").description("조회할 유저 ID"))
                                                       .responseHeaders(headerWithName(CONTENT_TYPE).description(APPLICATION_JSON_VALUE))
                                                       .responseFields(userResponseField())
                                                       .build())));
    // @formatter:on
  }

  @Test
  @DisplayName("DB에 유저를 등록한다.")
  void createUserBlockingTest() {
    UserRequest userRequest = new UserRequest("derrick rose", "rose@nhn.com");
    final User user = new User();
    user.setId(1L);
    user.setName("nhn");
    user.setEmail("nhn");
    user.setTeam(new Team());

    given(userService.save(any(UserRequest.class))).willReturn(Mono.just(user));

    // @formatter:off
    webTestClient.post()
                 .uri("/users/blocking")
                 .accept(APPLICATION_JSON)
                 .header(CLIENT_ID, "webflux")
                 .bodyValue(userRequest)
                 .exchange()
                 .expectStatus()
                 .isOk()
                 .expectHeader()
                 .contentType(APPLICATION_JSON)
                 .expectBody(User.class)
                 .consumeWith(document("create-user-blocking",
                                       resource(builder.tag("[User]")
                                                       .description("유저를 등록한다.")
                                                       .requestHeaders(headerWithName(CONTENT_TYPE).description(APPLICATION_JSON_VALUE),
                                                                       headerWithName(CLIENT_ID).description("클라이언트 ID").optional())
                                                       .requestFields(userRequestField())
                                                       .responseFields(new FieldDescriptor[] {
                                                         fieldWithPath("id").description("유저 ID").type(JsonFieldType.NUMBER),
                                                         fieldWithPath("name").description("이름").type(JsonFieldType.STRING),
                                                         fieldWithPath("email").description("이메일").type(JsonFieldType.STRING)
                                                       })
                                                       .build())));
  }

  @NotNull
  private FieldDescriptor[] userField() {
    return new FieldDescriptor[] {
      fieldWithPath("id").description("유저 ID").type(JsonFieldType.NUMBER),
      fieldWithPath("name").description("이름").type(JsonFieldType.STRING),
      fieldWithPath("email").description("이메일").type(JsonFieldType.STRING),
      fieldWithPath("teamId").description("팀 ID").type(JsonFieldType.NUMBER)
    };
  }

  @NotNull
  private FieldDescriptor[] userResponseField() {
    return new FieldDescriptor[] {
      fieldWithPath("id").description("유저 ID").type(JsonFieldType.NUMBER),
      fieldWithPath("name").description("이름").type(JsonFieldType.STRING),
      fieldWithPath("email").description("이메일").type(JsonFieldType.STRING),
      fieldWithPath("teamId").description("팀 ID").type(JsonFieldType.NUMBER),
      fieldWithPath("teamName").description("팀이름").type(JsonFieldType.STRING),
      fieldWithPath("companyId").description("회사 ID").type(JsonFieldType.NUMBER),
      fieldWithPath("companyName").description("회사").type(JsonFieldType.STRING)
    };
  }

  @NotNull
  private FieldDescriptor[] userRequestField() {
    return new FieldDescriptor[] {
      fieldWithPath("id").description("유저 ID").type(JsonFieldType.NUMBER),
      fieldWithPath("name").description("이름").type(JsonFieldType.STRING),
      fieldWithPath("email").description("이메일").type(JsonFieldType.STRING),
      fieldWithPath("teamId").description("팀 ID").type(JsonFieldType.NUMBER)
    };
  }
}