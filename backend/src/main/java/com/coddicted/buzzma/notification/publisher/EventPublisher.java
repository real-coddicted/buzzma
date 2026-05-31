package com.coddicted.buzzma.notification.publisher;

import com.coddicted.buzzma.notification.entity.EventType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {

  private static final Logger LOGGER = LoggerFactory.getLogger(EventPublisher.class);
  private static final String CHANNEL_PREFIX = "notif:user:";

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  public EventPublisher(final StringRedisTemplate redisTemplate, final ObjectMapper objectMapper) {
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
  }

  public void publishNotification(final UUID userId) {
    publish(userId, EventType.EVENT_TYPE_NOTIFICATION, "");
  }

  public void publishRefresh(final UUID userId, final String page) {
    publish(userId, EventType.EVENT_TYPE_REFRESH, page);
  }

  private void publish(final UUID userId, final EventType eventType, final String payload) {
    final String channel = CHANNEL_PREFIX + userId;
    final String message = serialize(eventType.name(), payload);
    if (message == null) {
      return;
    }
    try {
      this.redisTemplate.convertAndSend(channel, message);
    } catch (final Exception ex) {
      LOGGER.warn("Failed to publish {} event to {}: {}", eventType, channel, ex.getMessage());
    }
  }

  private String serialize(final String eventType, final String payload) {
    try {
      return this.objectMapper.writeValueAsString(
          Map.of("id", UUID.randomUUID().toString(), "type", eventType, "payload", payload));
    } catch (final JsonProcessingException ex) {
      LOGGER.warn("Failed to serialize notification event: {}", ex.getMessage());
      return null;
    }
  }
}
