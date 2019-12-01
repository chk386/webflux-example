package com.nhn.webflux.configuration;

import com.nhn.webflux.reactive.user.entity.User;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author haekyu cho
 */

@Configuration
@EnableRedisRepositories
public class RedisConfiguration {

  @Bean
  @Primary
  public ReactiveRedisTemplate<String, String> stringReactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
    return new ReactiveRedisTemplate<>(factory, RedisSerializationContext.string());
  }

  @Bean
  @Primary
  public ReactiveRedisTemplate<String, User> jsonReactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
    var key = new StringRedisSerializer();
    var value = new Jackson2JsonRedisSerializer<>(User.class);
    var context = RedisSerializationContext.<String, User>newSerializationContext().key(key)
                                                                                   .value(value)
                                                                                   .hashKey(key)
                                                                                   .hashValue(value)
                                                                                   .build();

    return new ReactiveRedisTemplate<>(factory, context);
  }

  @Bean
  public ReactiveRedisMessageListenerContainer container(ReactiveRedisConnectionFactory factory) {
    return new ReactiveRedisMessageListenerContainer(factory);
  }
}
