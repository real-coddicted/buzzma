package com.coddicted.buzzma.storage.event;

import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.storage.config.GoogleDriveProperties;
import com.coddicted.buzzma.storage.service.GoogleDriveService;
import com.coddicted.buzzma.storage.service.StorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Component
@ConditionalOnProperty(name = "app.storage.google-drive.enabled", havingValue = "true")
public class GoogleDriveUploadConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(GoogleDriveUploadConsumer.class);
  private static final String QUEUE_KEY = GoogleDriveUploadPublisher.QUEUE_KEY;
  private static final String DLQ_KEY = "queue:gdrive-upload:dlq";
  private static final Duration BRPOP_TIMEOUT = Duration.ofSeconds(30);

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;
  private final ClaimService claimService;
  private final StorageService storageService;
  private final GoogleDriveService googleDriveService;
  private final GoogleDriveProperties properties;
  private final Map<String, String> campaignFolderCache = new ConcurrentHashMap<>();
  private final AtomicBoolean running = new AtomicBoolean(true);
  private ExecutorService executor;

  public GoogleDriveUploadConsumer(
      final StringRedisTemplate redisTemplate,
      final ObjectMapper objectMapper,
      final ClaimService claimService,
      final StorageService storageService,
      final GoogleDriveService googleDriveService,
      final GoogleDriveProperties properties) {
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
    this.claimService = claimService;
    this.storageService = storageService;
    this.googleDriveService = googleDriveService;
    this.properties = properties;
  }

  @PostConstruct
  public void start() {
    this.executor =
        Executors.newSingleThreadExecutor(
            r -> {
              final Thread t = new Thread(r, "gdrive-upload-consumer");
              t.setDaemon(true);
              return t;
            });
    this.executor.submit(this::consumeLoop);
    LOGGER.info("Google Drive upload consumer started");
  }

  @PreDestroy
  public void stop() {
    this.running.set(false);
    if (this.executor != null) {
      this.executor.shutdownNow();
      try {
        this.executor.awaitTermination(5, TimeUnit.SECONDS);
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    LOGGER.info("Google Drive upload consumer stopped");
  }

  private void consumeLoop() {
    while (this.running.get() && !Thread.currentThread().isInterrupted()) {
      try {
        final String message = this.redisTemplate.opsForList().rightPop(QUEUE_KEY, BRPOP_TIMEOUT);

        if (message == null) {
          continue;
        }

        processMessage(message);
      } catch (final Exception e) {
        if (Thread.currentThread().isInterrupted()) {
          break;
        }
        LOGGER.error("Error in Google Drive upload consumer loop: {}", e.getMessage(), e);
      }
    }
  }

  private void processMessage(final String rawMessage) {
    final GoogleDriveUploadMessage message;
    try {
      message = this.objectMapper.readValue(rawMessage, GoogleDriveUploadMessage.class);
    } catch (final JsonProcessingException e) {
      LOGGER.warn("Invalid message in queue, cannot deserialize: {}", rawMessage);
      return;
    }

    try {
      uploadScreenshotToDrive(message);
    } catch (final Exception e) {
      LOGGER.error(
          "Failed to upload screenshot {} to Google Drive, moving to DLQ: {}",
          message.screenshotId(),
          e.getMessage(),
          e);
      this.redisTemplate.opsForList().leftPush(DLQ_KEY, rawMessage);
    }
  }

  @Transactional
  public void uploadScreenshotToDrive(final GoogleDriveUploadMessage message) {
    final String claimFolderId = resolveClaimFolder(message);

    final ResponseBytes<GetObjectResponse> fileBytes =
        this.storageService.retrieve(message.storageKey());
    final String contentType = fileBytes.response().contentType();
    final String filename =
        message.screenshotType().toLowerCase() + extensionFromKey(message.storageKey());

    final String driveUrl =
        this.googleDriveService.uploadFile(
            claimFolderId, filename, contentType, fileBytes.asByteArray());

    this.claimService.updateScreenshotGoogleDriveUrl(message.screenshotId(), driveUrl);

    LOGGER.info("Uploaded screenshot {} to Google Drive: {}", message.screenshotId(), driveUrl);
  }

  private String resolveClaimFolder(final GoogleDriveUploadMessage message) {
    final String campaignFolderId =
        this.campaignFolderCache.computeIfAbsent(
            message.campaignCode(),
            code ->
                this.googleDriveService.findOrCreateFolder(
                    this.properties.getRootFolderId(), code));

    return this.googleDriveService.findOrCreateFolder(campaignFolderId, message.claimCode());
  }

  private String extensionFromKey(final String storageKey) {
    final int dot = storageKey.lastIndexOf('.');
    return dot >= 0 ? storageKey.substring(dot) : "";
  }
}
