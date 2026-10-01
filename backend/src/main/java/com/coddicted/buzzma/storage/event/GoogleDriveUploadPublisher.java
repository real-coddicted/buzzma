package com.coddicted.buzzma.storage.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.storage.google-drive.enabled", havingValue = "true")
public class GoogleDriveUploadPublisher {

  private static final Logger LOGGER = LoggerFactory.getLogger(GoogleDriveUploadPublisher.class);
  static final String QUEUE_KEY = "queue:gdrive-upload";

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  public GoogleDriveUploadPublisher(
      final StringRedisTemplate redisTemplate, final ObjectMapper objectMapper) {
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
  }

  public void enqueue(final GoogleDriveUploadMessage message) {
    try {
      final String json = this.objectMapper.writeValueAsString(message);
      this.redisTemplate.opsForList().leftPush(QUEUE_KEY, json);
      LOGGER.debug("Enqueued Google Drive upload for screenshot {}", message.screenshotId());
    } catch (final JsonProcessingException e) {
      LOGGER.error(
          "Failed to serialize Google Drive upload message for screenshot {}: {}",
          message.screenshotId(),
          e.getMessage());
    }
  }
}
