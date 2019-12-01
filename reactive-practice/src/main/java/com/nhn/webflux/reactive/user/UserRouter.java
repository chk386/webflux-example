package com.nhn.webflux.reactive.user;

import com.nhn.webflux.reactive.user.handler.UserHandler;
import com.nhn.webflux.reactive.user.handler.UserHandlerBlocking;
import com.nhn.webflux.reactive.user.handler.UserHandlerRedis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class UserRouter {

  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private final UserHandler userHandler;
  private final UserHandlerBlocking userHandlerBlocking;
  private final UserHandlerRedis userHandlerRedis;

  public UserRouter(UserHandler userHandler, UserHandlerBlocking userHandlerBlocking,
                    UserHandlerRedis userHandlerRedis) {
    this.userHandler = userHandler;
    this.userHandlerBlocking = userHandlerBlocking;
    this.userHandlerRedis = userHandlerRedis;
  }

  @Bean
  public RouterFunction<ServerResponse> userRoute() {
    // todo: 코드 작성
    return null;
  }
}
