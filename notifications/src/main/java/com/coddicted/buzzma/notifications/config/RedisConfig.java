package com.coddicted.buzzma.notifications.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

  @Bean
  public ReactiveRedisMessageListenerContainer reactiveRedisMessageListenerContainer(
      final ReactiveRedisConnectionFactory connectionFactory) {
    return new ReactiveRedisMessageListenerContainer(connectionFactory);
  }

  @Bean
  public ReactiveRedisTemplate<String, String> reactiveStringRedisTemplate(
      final ReactiveRedisConnectionFactory connectionFactory) {
    final StringRedisSerializer serializer = new StringRedisSerializer();
    final RedisSerializationContext<String, String> context =
        RedisSerializationContext.<String, String>newSerializationContext(serializer)
            .value(serializer)
            .build();
    return new ReactiveRedisTemplate<>(connectionFactory, context);
  }
}
