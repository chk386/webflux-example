package com.nhn.webflux.reactive.user;

import com.nhn.webflux.reactive.user.handler.UserHandler;
import com.nhn.webflux.reactive.user.handler.UserHandlerBlocking;
import com.nhn.webflux.reactive.user.handler.UserHandlerRedis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.function.Consumer;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

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

    return route().path("/users",
                        b1 -> b1.GET("/{id}", userHandler::getUser)
                                .POST("/", contentType(APPLICATION_JSON), userHandler::createUser)
                                .GET("/blocking/{id}", userHandlerBlocking::getUser)
                                .POST("/blocking", contentType(APPLICATION_JSON), userHandlerBlocking::createUser))
                  .build();
  }
}
