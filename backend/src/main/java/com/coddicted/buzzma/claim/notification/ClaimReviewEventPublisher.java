package com.coddicted.buzzma.claim.notification;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ScreenshotType;
import com.coddicted.buzzma.claim.entity.ScreenshotVerificationStatus;
import com.coddicted.buzzma.notification.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ClaimReviewEventPublisher {

  private final NotificationService notificationService;

  public ClaimReviewEventPublisher(final NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @Async
  public void publishScreenshotReviewedEvent(
      final Claim claim,
      final Deal deal,
      final ScreenshotType screenshotType,
      final ScreenshotVerificationStatus status,
      final UUID reviewerId,
      final String reviewerComments) {
    log.info("Publishing screenshot reviewed event ({}) for claim id: {}", status, claim.getId());
    final Campaign campaign = deal.getCampaign();
    final StringBuilder message =
        new StringBuilder(campaign.getTitle())
            .append(" (")
            .append(deal.getCode())
            .append(") - ")
            .append(screenshotType.getDisplayName())
            .append(" screenshot ")
            .append(status.getDisplayName());
    if (reviewerComments != null && !reviewerComments.isBlank()) {
      message.append(": ").append(reviewerComments);
    }
    this.notificationService.create(
        "Screenshot " + status.getDisplayName(),
        message.toString(),
        List.of(claim.getOwnerId(), deal.getOwnerId()),
        reviewerId);
  }
}
