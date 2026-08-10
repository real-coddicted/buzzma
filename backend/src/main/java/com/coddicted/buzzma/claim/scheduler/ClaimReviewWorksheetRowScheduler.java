package com.coddicted.buzzma.claim.scheduler;

import com.coddicted.buzzma.claim.config.ClaimReviewWorksheetSchedulerProperties;
import com.coddicted.buzzma.claim.entity.ClaimReviewWorksheetRow;
import com.coddicted.buzzma.claim.persistence.ClaimReviewWorksheetRowRepository;
import com.coddicted.buzzma.claim.service.ClaimReviewWorksheetRowService;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ClaimReviewWorksheetRowScheduler {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(ClaimReviewWorksheetRowScheduler.class);

  private final ClaimReviewWorksheetRowRepository rowRepository;
  private final ClaimReviewWorksheetRowService rowService;
  private final ClaimReviewWorksheetSchedulerProperties properties;

  public ClaimReviewWorksheetRowScheduler(
      final ClaimReviewWorksheetRowRepository rowRepository,
      final ClaimReviewWorksheetRowService rowService,
      final ClaimReviewWorksheetSchedulerProperties properties) {
    this.rowRepository = rowRepository;
    this.rowService = rowService;
    this.properties = properties;
  }

  @Scheduled(fixedDelayString = "${app.claim-review.worksheet.scheduler.fixed-delay-ms:60000}")
  public void processWorksheetRows() {
    int reset = rowRepository.resetStaleInProgressRows(properties.getStaleThresholdMinutes());
    if (reset > 0) {
      LOGGER.warn(
          "ClaimReviewWorksheetRowScheduler: reset {} stale IN_PROGRESS row(s) (>{} min) to PENDING",
          reset,
          properties.getStaleThresholdMinutes());
    }

    final List<UUID> claimedIds =
        rowService.claimBatchForProcessing(properties.getBatchSize(), properties.getMaxRetries());
    if (claimedIds.isEmpty()) {
      return;
    }

    LOGGER.info("ClaimReviewWorksheetRowScheduler: processing {} row(s)", claimedIds.size());

    final List<ClaimReviewWorksheetRow> rows = rowRepository.findAllById(claimedIds);
    rows.forEach(
        row -> {
          try {
            rowService.processRow(row);
          } catch (final Exception e) {
            LOGGER.warn(
                "ClaimReviewWorksheetRowScheduler: failed for row {}: {}",
                row.getId(),
                e.getMessage(),
                e);
            if (row.getRetryCount() + 1 < properties.getMaxRetries()) {
              rowService.resetForRetry(row.getId());
            } else {
              rowService.markFailed(row.getId(), e.getMessage());
              LOGGER.error(
                  "ClaimReviewWorksheetRowScheduler: max retries ({}) exceeded for row {} — marked ERROR, needs manual review",
                  properties.getMaxRetries(),
                  row.getId());
            }
          }
        });
  }
}
