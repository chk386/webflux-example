package com.nhn.webflux.reactive.address;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import reactor.core.publisher.Mono;

@Component
public class AddressHandler {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  public Mono<String> getAddress(ServerRequest request) {
    // https://alpha-api.e-ncp.com/index.html#/[Common]_%3E_Addresses/getAddressesUsingGET
    // todo : curl -X GET "http://alpha-api.e-ncp.com/addresses/search?keyword=NHN" -H "accept: */*" -H "clientId: f7IuuZPHwmc7hGfGmbhHog==" -H "platform: PC"
    // todo : 응답body를 logger로 출력하세요.
    // todo : Ex3AddressWebClientTest를 구현하세요.
    return null;
  }
}
