package com.coddicted.buzzma.storage.event;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.persistence.ClaimRepository;
import com.coddicted.buzzma.claim.persistence.ClaimScreenshotRepository;
import com.coddicted.buzzma.storage.config.GoogleDriveProperties;
import com.coddicted.buzzma.storage.service.GoogleDriveService;
import com.coddicted.buzzma.storage.service.StorageService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
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
  private final ClaimScreenshotRepository claimScreenshotRepository;
  private final ClaimRepository claimRepository;
  private final CampaignService campaignService;
  private final StorageService storageService;
  private final GoogleDriveService googleDriveService;
  private final GoogleDriveProperties properties;
  private final Map<String, String> folderIdCache = new ConcurrentHashMap<>();
  private final AtomicBoolean running = new AtomicBoolean(true);
  private ExecutorService executor;

  public GoogleDriveUploadConsumer(
      final StringRedisTemplate redisTemplate,
      final ClaimScreenshotRepository claimScreenshotRepository,
      final ClaimRepository claimRepository,
      final CampaignService campaignService,
      final StorageService storageService,
      final GoogleDriveService googleDriveService,
      final GoogleDriveProperties properties) {
    this.redisTemplate = redisTemplate;
    this.claimScreenshotRepository = claimScreenshotRepository;
    this.claimRepository = claimRepository;
    this.campaignService = campaignService;
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

  private void processMessage(final String message) {
    final UUID screenshotId;
    try {
      screenshotId = UUID.fromString(message.trim());
    } catch (final IllegalArgumentException e) {
      LOGGER.warn("Invalid screenshot ID in queue: {}", message);
      return;
    }

    try {
      uploadScreenshotToDrive(screenshotId);
    } catch (final Exception e) {
      LOGGER.error(
          "Failed to upload screenshot {} to Google Drive, moving to DLQ: {}",
          screenshotId,
          e.getMessage(),
          e);
      this.redisTemplate.opsForList().leftPush(DLQ_KEY, message);
    }
  }

  @Transactional
  public void uploadScreenshotToDrive(final UUID screenshotId) {
    final ClaimScreenshot screenshot =
        this.claimScreenshotRepository.findById(screenshotId).orElse(null);

    if (screenshot == null || screenshot.isDeleted()) {
      LOGGER.warn("Screenshot {} not found or deleted, skipping", screenshotId);
      return;
    }

    if (screenshot.getGoogleDriveUrl() != null) {
      this.googleDriveService.deleteFile(screenshot.getGoogleDriveUrl());
    }

    final Claim claim =
        this.claimRepository
            .findById(screenshot.getClaimId())
            .orElseThrow(
                () -> new IllegalStateException("Claim not found for screenshot: " + screenshotId));

    final Campaign campaign = this.campaignService.getById(claim.getCampaignId());

    final String claimFolderId = resolveClaimFolder(campaign, claim);

    final ResponseBytes<GetObjectResponse> fileBytes =
        this.storageService.retrieve(screenshot.getStorageKey());
    final String contentType = fileBytes.response().contentType();
    final String filename =
        screenshot.getType().name().toLowerCase() + extensionFromKey(screenshot.getStorageKey());

    final String driveUrl =
        this.googleDriveService.uploadFile(
            claimFolderId, filename, contentType, fileBytes.asByteArray());

    screenshot.setGoogleDriveUrl(driveUrl);
    this.claimScreenshotRepository.save(screenshot);

    LOGGER.info("Uploaded screenshot {} to Google Drive: {}", screenshotId, driveUrl);
  }

  private String resolveClaimFolder(final Campaign campaign, final Claim claim) {
    if (claim.getGoogleDriveFolderId() != null) {
      return claim.getGoogleDriveFolderId();
    }

    final String campaignFolderCacheKey = "campaign:" + campaign.getId();
    final String campaignFolderId =
        this.folderIdCache.computeIfAbsent(
            campaignFolderCacheKey,
            k ->
                this.googleDriveService.findOrCreateFolder(
                    this.properties.getRootFolderId(), campaign.getCode()));

    final String claimFolderId =
        this.googleDriveService.findOrCreateFolder(campaignFolderId, claim.getCode());

    claim.setGoogleDriveFolderId(claimFolderId);
    this.claimRepository.save(claim);

    return claimFolderId;
  }

  private String extensionFromKey(final String storageKey) {
    final int dot = storageKey.lastIndexOf('.');
    return dot >= 0 ? storageKey.substring(dot) : "";
  }
}
