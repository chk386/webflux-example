package com.nhn.webflux.reactive.user;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder;
import com.nhn.webflux.reactive.user.handler.UserHandler;
import com.nhn.webflux.reactive.user.handler.UserHandlerBlocking;
import com.nhn.webflux.reactive.user.model.UserRequest;
import com.nhn.webflux.reactive.user.repository.UserRepository;

import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestBody;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestPartBody;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;

@ExtendWith({RestDocumentationExtension.class})
@WebFluxTest()
@ContextConfiguration(classes = {UserRouter.class, UserHandler.class})
@AutoConfigureRestDocs
class UserHandlerTest {

  @MockBean
  UserHandlerBlocking userHandlerBlocking;

  @MockBean
  UserRepository userRepository;

  @Autowired
  private WebTestClient webTestClient;
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
    webTestClient.post()
                 .uri("/users/bulk")
                 .accept(MULTIPART_FORM_DATA)
                 .header(CLIENT_ID, "webflux")
                 .body(BodyInserters.fromMultipartData("sample.txt", resource))
                 .exchange()
                 .expectStatus()
                 .isOk()
                 .expectHeader()
                 .valueEquals(CONTENT_TYPE, TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
                 .expectBody()
                 .consumeWith(document("bulk-user",requestParts(partWithName(resource.getFilename()).description("The file to upload"))));
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
                 .expectStatus()
                 .isCreated()
                 .expectHeader()
                 .contentType(APPLICATION_JSON)
                 .expectHeader()
                 .value(LOCATION, Matchers.containsString("users"))
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
}