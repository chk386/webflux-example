package com.nhn.webflux.reactive.address;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class AddressRouter {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Bean
  public RouterFunction<ServerResponse> addressRoute() {
    // todo: /address/search?keyword=xxx
    return null;
  }
}
