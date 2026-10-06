package com.coddicted.buzzma.storage.scheduler;

import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.storage.config.R2Properties;
import com.coddicted.buzzma.storage.event.R2UploadMessage;
import com.coddicted.buzzma.storage.event.R2UploadPublisher;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Re-queues screenshots that still have no R2 public URL: ones uploaded before R2 existed, ones
 * whose upload failed, and ones whose queue message was lost.
 */
@Component
@ConditionalOnProperty(name = "app.storage.r2.enabled", havingValue = "true")
public class R2UploadRetryScheduler {

  private static final Logger LOGGER = LoggerFactory.getLogger(R2UploadRetryScheduler.class);

  private final ClaimService claimService;
  private final R2UploadPublisher r2UploadPublisher;
  private final R2Properties properties;
  private final Duration minAge;
  private final int batchSize;

  public R2UploadRetryScheduler(
      final ClaimService claimService,
      final R2UploadPublisher r2UploadPublisher,
      final R2Properties properties,
      @Value("${app.storage.r2.retry.min-age-minutes:30}") final int minAgeMinutes,
      @Value("${app.storage.r2.retry.batch-size:500}") final int batchSize) {
    this.claimService = claimService;
    this.r2UploadPublisher = r2UploadPublisher;
    this.properties = properties;
    this.minAge = Duration.ofMinutes(minAgeMinutes);
    this.batchSize = batchSize;
  }

  @Scheduled(
      fixedDelayString = "${app.storage.r2.retry.fixed-delay-ms:1800000}",
      initialDelayString = "${app.storage.r2.retry.initial-delay-ms:60000}")
  public void requeuePendingUploads() {
    requeuePendingUploads(Instant.now());
  }

  void requeuePendingUploads(final Instant now) {
    final List<R2UploadMessage> pending =
        this.claimService.listPendingR2Uploads(
            now.minus(this.minAge), this.properties.getMaxUploadAttempts(), this.batchSize);
    if (pending.isEmpty()) {
      return;
    }
    pending.forEach(this.r2UploadPublisher::enqueue);
    LOGGER.info("R2UploadRetryScheduler: re-queued {} screenshot(s) for R2 upload", pending.size());
  }
}
