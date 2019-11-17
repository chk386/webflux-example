package com.nhn.webflux.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.Map;
import java.util.Objects;

/**
 * <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html#webflux-websocket">reactive websocket</a>
 */
@Configuration
public class WebSocketConfiguration {

  private final Logger logger = LoggerFactory.getLogger(WebSocketConfiguration.class);

  private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
  private final ReactiveRedisMessageListenerContainer listenerContainer;

  public WebSocketConfiguration(ReactiveRedisTemplate<String, String> reactiveRedisTemplate,
                                ReactiveRedisMessageListenerContainer listenerContainer) {
    this.reactiveRedisTemplate = reactiveRedisTemplate;
    this.listenerContainer = listenerContainer;
  }

  @Bean
  public WebSocketHandlerAdapter handlerAdapter() {
    return new WebSocketHandlerAdapter();
  }

  @Bean
  public HandlerMapping handlerMapping() {
    var urlMap = Map.of("/ws", echoHandler(), "/pubsub", pubsub());

    return new SimpleUrlHandlerMapping(urlMap, -1);
  }

  private WebSocketHandler pubsub() {
    return session -> {
      session.receive()
             .subscribe(v -> reactiveRedisTemplate.convertAndSend("webflux", v.getPayloadAsText())
                                                  .subscribe());

      return session.send(listenerContainer.receive(new ChannelTopic("webflux"))
                                           .map(channelMessage -> {
                                             var hostAddress = Objects.requireNonNull(session.getHandshakeInfo()
                                                                                             .getRemoteAddress())
                                                                      .getAddress()
                                                                      .getHostName();
                                             var text = "[" + hostAddress + "] : " + channelMessage.getMessage();

                                             return session.textMessage(text);
                                           }));
    };
  }

  private WebSocketHandler echoHandler() {
    return session -> {
      var output = session.receive()
                          .map(message -> {
                            var payloadAsText = message.getPayloadAsText();
                            logger.info("payload : {}", payloadAsText);

                            return session.textMessage("echo :" + payloadAsText);
                          });

      return session.send(output);
    };
  }
}
