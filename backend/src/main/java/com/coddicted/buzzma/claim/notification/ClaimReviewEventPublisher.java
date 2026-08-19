package com.coddicted.buzzma.claim.notification;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimStatus;
import com.coddicted.buzzma.claim.entity.ScreenshotType;
import com.coddicted.buzzma.claim.entity.ScreenshotVerificationStatus;
import com.coddicted.buzzma.notification.service.NotificationService;
import com.coddicted.buzzma.notification.template.NotificationTemplateRenderer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ClaimReviewEventPublisher {

  private static final String APPROVED_TITLE_KEY = "claim-decision-notification.approved.title";
  private static final String APPROVED_TITLE_DEFAULT = "Hurray! Claim {claimCode} approved!!";
  private static final String APPROVED_MESSAGE_KEY = "claim-decision-notification.approved.message";
  private static final String APPROVED_MESSAGE_DEFAULT =
      "Congratulations! Your claim with code {claimCode} has been approved. We will keep you"
          + " updated once payment is released for it.";
  private static final String REJECTED_TITLE_KEY = "claim-decision-notification.rejected.title";
  private static final String REJECTED_TITLE_DEFAULT = "Claim {claimCode} rejected!";
  private static final String REJECTED_MESSAGE_KEY = "claim-decision-notification.rejected.message";
  private static final String REJECTED_MESSAGE_DEFAULT =
      "Your claim with code {claimCode} has been rejected with reason: {reviewerComment}. Please"
          + " visit claims page for more details or contact your mediator.";

  private final NotificationService notificationService;
  private final NotificationTemplateRenderer templateRenderer;

  public ClaimReviewEventPublisher(
      final NotificationService notificationService,
      final NotificationTemplateRenderer templateRenderer) {
    this.notificationService = notificationService;
    this.templateRenderer = templateRenderer;
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

  @Async
  public void publishClaimDecisionEvent(
      final Claim claim, final ClaimStatus status, final String reviewerComments) {
    log.info("Publishing claim decision event ({}) for claim id: {}", status, claim.getId());
    final String title;
    final String message;
    if (status == ClaimStatus.APPROVED) {
      final Map<String, String> placeholders = Map.of("claimCode", claim.getCode());
      title = templateRenderer.render(APPROVED_TITLE_KEY, APPROVED_TITLE_DEFAULT, placeholders);
      message =
          templateRenderer.render(APPROVED_MESSAGE_KEY, APPROVED_MESSAGE_DEFAULT, placeholders);
    } else {
      final Map<String, String> placeholders =
          Map.of(
              "claimCode", claim.getCode(),
              "reviewerComment", String.valueOf(reviewerComments));
      title = templateRenderer.render(REJECTED_TITLE_KEY, REJECTED_TITLE_DEFAULT, placeholders);
      message =
          templateRenderer.render(REJECTED_MESSAGE_KEY, REJECTED_MESSAGE_DEFAULT, placeholders);
    }
    this.notificationService.create(title, message, claim.getOwnerId(), claim.getReviewerId());
  }

  private static String formatPaise(final BigInteger paise) {
    return new BigDecimal(paise)
        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
        .toPlainString();
  }
}
