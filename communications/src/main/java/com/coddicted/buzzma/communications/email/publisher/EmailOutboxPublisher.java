package com.coddicted.buzzma.communications.email.publisher;

import com.coddicted.buzzma.communications.email.constant.WellKnownQueues;
import com.coddicted.buzzma.communications.email.model.EmailOutboxMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class EmailOutboxPublisher {

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  public EmailOutboxPublisher(
      final StringRedisTemplate redisTemplate, final ObjectMapper objectMapper) {
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
  }

  public void publish(
      final UUID requestId, final String to, final String subject, final String body) {
    final EmailOutboxMessage message =
        EmailOutboxMessage.builder()
            .requestId(requestId)
            .to(to)
            .subject(subject)
            .body(body)
            .build();
    try {
      this.redisTemplate
          .opsForList()
          .leftPush(
              WellKnownQueues.EMAIL_OUTBOX_QUEUE_KEY,
              this.objectMapper.writeValueAsString(message));
    } catch (final JsonProcessingException ex) {
      throw new IllegalStateException("Failed to serialize email outbox message", ex);
    }
  }
}
