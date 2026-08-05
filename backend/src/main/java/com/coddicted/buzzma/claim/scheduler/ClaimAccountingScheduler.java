package com.coddicted.buzzma.claim.scheduler;

import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.persistence.ClaimRepository;
import com.coddicted.buzzma.claim.service.ClaimAccountingService;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ClaimAccountingScheduler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClaimAccountingScheduler.class);

  private final ClaimRepository claimRepository;
  private final ClaimAccountingService claimAccountingService;
  private final int batchSize;
  private final int maxRetries;
  private final int staleThresholdMinutes;

  public ClaimAccountingScheduler(
      final ClaimRepository claimRepository,
      final ClaimAccountingService claimAccountingService,
      @Value("${app.claim.accounting.scheduler.batch-size:50}") final int batchSize,
      @Value("${app.claim.accounting.scheduler.max-retries:3}") final int maxRetries,
      @Value("${app.claim.accounting.scheduler.stale-threshold-minutes:30}")
          final int staleThresholdMinutes) {
    this.claimRepository = claimRepository;
    this.claimAccountingService = claimAccountingService;
    this.batchSize = batchSize;
    this.maxRetries = maxRetries;
    this.staleThresholdMinutes = staleThresholdMinutes;
  }

  @Scheduled(fixedDelayString = "${app.claim.accounting.scheduler.fixed-delay-ms:300000}")
  public void processApprovedClaims() {
    int reset = claimRepository.resetStaleInProgressClaims(staleThresholdMinutes);
    if (reset > 0) {
      LOGGER.warn(
          "ClaimAccountingScheduler: reset {} stale IN_PROGRESS claim(s) (>{} min) to PENDING",
          reset,
          staleThresholdMinutes);
    }

    final List<UUID> claimedIds =
        claimAccountingService.claimBatchForProcessing(batchSize, maxRetries);
    if (claimedIds.isEmpty()) {
      return;
    }

    LOGGER.info("ClaimAccountingScheduler: processing {} claimed claim(s)", claimedIds.size());

    final List<Claim> claims = claimRepository.findAllById(claimedIds);
    claims.forEach(
        claim -> {
          try {
            claimAccountingService.processAccounting(claim);
          } catch (final Exception e) {
            LOGGER.error(
                "ClaimAccountingScheduler: failed for claim {}: {}",
                claim.getId(),
                e.getMessage(),
                e);
            if (claim.getAccountingRetryCount() + 1 < maxRetries) {
              claimRepository.resetAccountingForRetry(claim.getId());
            } else {
              claimRepository.markAccountingFailed(claim.getId());
              LOGGER.error(
                  "ClaimAccountingScheduler: max retries ({}) exceeded for claim {} — marked FAILED, needs manual review",
                  maxRetries,
                  claim.getId());
            }
          }
        });
  }
}
