package com.coddicted.buzzma.storage.event;

import java.util.UUID;
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

  public GoogleDriveUploadPublisher(final StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  public void enqueue(final UUID claimScreenshotId) {
    this.redisTemplate.opsForList().leftPush(QUEUE_KEY, claimScreenshotId.toString());
    LOGGER.debug("Enqueued Google Drive upload for screenshot {}", claimScreenshotId);
  }
}
