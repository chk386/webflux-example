package com.nhn.webflux.reactive.user;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder;
import com.nhn.webflux.reactive.team.repository.TeamRepository;
import com.nhn.webflux.reactive.user.handler.UserHandler;
import com.nhn.webflux.reactive.user.handler.UserHandlerBlocking;
import com.nhn.webflux.reactive.user.handler.UserHandlerRedis;
import com.nhn.webflux.reactive.user.repository.UserRepository;
import com.nhn.webflux.reactive.user.service.UserService;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

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
  UserRepository userRepository;

  @MockBean
  TeamRepository teamRepository;

  @MockBean
  UserService userService;

  @Autowired
  private WebTestClient webTestClient;
  private ResourceSnippetParametersBuilder builder = ResourceSnippetParameters.builder();

  @Test
  @DisplayName("유저를 조회한다.")
  void getUser() {
  }

  @Test
  @DisplayName("유저를 등록한다.")
  void createUser() {
  }

  @Test
  @DisplayName("DB에서 유저를 가져온다.")
  void getUserBlockingTest() {
  }

  @Test
  @DisplayName("DB에 유저를 등록한다.")
  void createUserBlockingTest() {
  }

  @NotNull
  private FieldDescriptor[] userField() {
    return new FieldDescriptor[]{fieldWithPath("id").description("유저 ID").type(JsonFieldType.NUMBER),
      fieldWithPath("name").description("이름").type(JsonFieldType.STRING),
      fieldWithPath("email").description("이메일").type(JsonFieldType.STRING),
      fieldWithPath("teamId").description("팀 ID").type(JsonFieldType.NUMBER)};
  }

  @NotNull
  private FieldDescriptor[] userResponseField() {
    return new FieldDescriptor[]{fieldWithPath("id").description("유저 ID").type(JsonFieldType.NUMBER),
      fieldWithPath("name").description("이름").type(JsonFieldType.STRING),
      fieldWithPath("email").description("이메일").type(JsonFieldType.STRING),
      fieldWithPath("teamId").description("팀 ID").type(JsonFieldType.NUMBER),
      fieldWithPath("teamName").description("팀이름").type(JsonFieldType.STRING),
      fieldWithPath("companyId").description("회사 ID").type(JsonFieldType.NUMBER),
      fieldWithPath("companyName").description("회사").type(JsonFieldType.STRING)};
  }

  @NotNull
  private FieldDescriptor[] userRequestField() {
    return new FieldDescriptor[]{fieldWithPath("id").description("유저 ID").type(JsonFieldType.NUMBER),
      fieldWithPath("name").description("이름").type(JsonFieldType.STRING),
      fieldWithPath("email").description("이메일").type(JsonFieldType.STRING),
      fieldWithPath("teamId").description("팀 ID").type(JsonFieldType.NUMBER)};
  }
}