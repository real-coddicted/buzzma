package com.coddicted.buzzma.notifications.sse;

import com.coddicted.buzzma.notifications.dto.NotificationEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class NotificationsStreamService {

  private static final Logger LOGGER = LoggerFactory.getLogger(NotificationsStreamService.class);
  private static final String CHANNEL_PREFIX = "notif:user:";

  private final ReactiveRedisMessageListenerContainer listenerContainer;
  private final ObjectMapper objectMapper;

  public NotificationsStreamService(
      final ReactiveRedisMessageListenerContainer listenerContainer,
      final ObjectMapper objectMapper) {
    this.listenerContainer = listenerContainer;
    this.objectMapper = objectMapper;
  }

  public Flux<NotificationEvent> streamFor(final UUID userId) {
    final String channel = CHANNEL_PREFIX + userId;
    LOGGER.debug("Subscribing to {}", channel);
    return this.listenerContainer
        .receive(ChannelTopic.of(channel))
        .map(message -> deserialize(message.getMessage()))
        .doOnCancel(() -> LOGGER.debug("Unsubscribed from {}", channel));
  }

  private NotificationEvent deserialize(final String payload) {
    try {
      return this.objectMapper.readValue(payload, NotificationEvent.class);
    } catch (JsonProcessingException ex) {
      LOGGER.warn("Dropping malformed notification payload: {}", ex.getOriginalMessage());
      return NotificationEvent.builder().type("invalid").payload(payload).build();
    }
  }
}
