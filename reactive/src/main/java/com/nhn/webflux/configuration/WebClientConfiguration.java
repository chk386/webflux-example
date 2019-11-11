package com.nhn.webflux.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;

import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;

/**
 * @author haekyu.cho@nhnent.com
 */
@Configuration
public class WebClientConfiguration {

    @Bean
    public WebClient webClient() {
        HttpClient client = HttpClient.create()
                                      .keepAlive(false)
                                      .tcpConfiguration(c -> c.option(CONNECT_TIMEOUT_MILLIS, 10000)
                                                              .doOnConnected(conn -> {
                                                                  conn.addHandler(new ReadTimeoutHandler(10));
                                                                  conn.addHandler(new WriteTimeoutHandler(10));
                                                              }));

        return WebClient.builder()
                        .clientConnector(new ReactorClientHttpConnector(client))
                        .build();
    }
}
