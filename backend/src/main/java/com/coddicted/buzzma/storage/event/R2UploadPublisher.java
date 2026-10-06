package com.coddicted.buzzma.storage.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.storage.r2.enabled", havingValue = "true")
public class R2UploadPublisher {

  private static final Logger LOGGER = LoggerFactory.getLogger(R2UploadPublisher.class);
  static final String QUEUE_KEY = "queue:r2-upload";

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  public R2UploadPublisher(
      final StringRedisTemplate redisTemplate, final ObjectMapper objectMapper) {
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
  }

  public void enqueue(final R2UploadMessage message) {
    try {
      final String json = this.objectMapper.writeValueAsString(message);
      this.redisTemplate.opsForList().leftPush(QUEUE_KEY, json);
      LOGGER.debug("Enqueued R2 upload for screenshot {}", message.screenshotId());
    } catch (final JsonProcessingException e) {
      LOGGER.error(
          "Failed to serialize R2 upload message for screenshot {}: {}",
          message.screenshotId(),
          e.getMessage());
    }
  }
}
