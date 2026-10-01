package com.coddicted.buzzma.storage.event;

import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.storage.config.R2Properties;
import com.coddicted.buzzma.storage.service.StorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
@ConditionalOnProperty(name = "app.storage.r2.enabled", havingValue = "true")
public class R2UploadConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(R2UploadConsumer.class);
  private static final String QUEUE_KEY = R2UploadPublisher.QUEUE_KEY;
  private static final Duration BRPOP_TIMEOUT = Duration.ofSeconds(30);

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;
  private final ClaimService claimService;
  private final StorageService storageService;
  private final S3Client r2Client;
  private final R2Properties properties;
  private final AtomicBoolean running = new AtomicBoolean(true);
  private ExecutorService executor;

  public R2UploadConsumer(
      final StringRedisTemplate redisTemplate,
      final ObjectMapper objectMapper,
      final ClaimService claimService,
      final StorageService storageService,
      @Qualifier("r2Client") final S3Client r2Client,
      final R2Properties properties) {
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
    this.claimService = claimService;
    this.storageService = storageService;
    this.r2Client = r2Client;
    this.properties = properties;
  }

  @PostConstruct
  public void start() {
    this.executor =
        Executors.newSingleThreadExecutor(
            r -> {
              final Thread t = new Thread(r, "r2-upload-consumer");
              t.setDaemon(true);
              return t;
            });
    this.executor.submit(this::consumeLoop);
    LOGGER.info("R2 upload consumer started");
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
    LOGGER.info("R2 upload consumer stopped");
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
        LOGGER.error("Error in R2 upload consumer loop: {}", e.getMessage(), e);
      }
    }
  }

  void processMessage(final String rawMessage) {
    final R2UploadMessage message;
    try {
      message = this.objectMapper.readValue(rawMessage, R2UploadMessage.class);
    } catch (final JsonProcessingException e) {
      LOGGER.warn("Invalid message in queue, cannot deserialize: {}", rawMessage);
      return;
    }

    try {
      uploadScreenshotToR2(message);
    } catch (final Exception e) {
      recordFailedAttempt(message, e);
    }
  }

  private void recordFailedAttempt(final R2UploadMessage message, final Exception cause) {
    final int attempts =
        this.claimService.incrementScreenshotR2UploadAttempts(message.screenshotId());
    final int maxAttempts = this.properties.getMaxUploadAttempts();
    if (attempts >= maxAttempts) {
      LOGGER.error(
          "R2 upload for screenshot {} failed on final attempt {}/{}, giving up: {}",
          message.screenshotId(),
          attempts,
          maxAttempts,
          cause.getMessage(),
          cause);
    } else {
      LOGGER.warn(
          "R2 upload for screenshot {} failed on attempt {}/{}, will retry: {}",
          message.screenshotId(),
          attempts,
          maxAttempts,
          cause.getMessage());
    }
  }

  @Transactional
  public void uploadScreenshotToR2(final R2UploadMessage message) {
    final ResponseBytes<GetObjectResponse> fileBytes =
        this.storageService.retrieve(message.storageKey());
    final String contentType = fileBytes.response().contentType();
    final String extension = extensionFromKey(message.storageKey());
    final String filename = message.screenshotType().toLowerCase() + extension;

    final String r2Key = message.campaignCode() + "/" + message.claimCode() + "/" + filename;

    final PutObjectRequest putRequest =
        PutObjectRequest.builder()
            .bucket(this.properties.getBucket())
            .key(r2Key)
            .contentType(contentType)
            .build();

    this.r2Client.putObject(putRequest, RequestBody.fromBytes(fileBytes.asByteArray()));

    final String publicUrl = buildPublicUrl(r2Key);
    this.claimService.updateScreenshotPublicUrl(message.screenshotId(), publicUrl);

    LOGGER.info("Uploaded screenshot {} to R2: {}", message.screenshotId(), publicUrl);
  }

  private String buildPublicUrl(final String key) {
    String base = this.properties.getPublicUrlBase();
    if (base.endsWith("/")) {
      base = base.substring(0, base.length() - 1);
    }
    return base + "/" + key;
  }

  private String extensionFromKey(final String storageKey) {
    final int dot = storageKey.lastIndexOf('.');
    return dot >= 0 ? storageKey.substring(dot) : "";
  }
}
