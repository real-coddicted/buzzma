package com.coddicted.buzzma.claim.notification;

import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_REVIEW;
import static com.coddicted.buzzma.claim.entity.ScreenshotVerificationStatus.SCREENSHOT_VERIFICATION_STATUS_REJECTED;
import static org.mockito.Mockito.verify;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.notification.service.NotificationService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClaimReviewEventPublisherTest {

  private static final UUID BUYER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID MEDIATOR_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID REVIEWER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
  private static final String REVIEWER_COMMENTS = "Screenshot does not match the order";

  @Mock private NotificationService mockNotificationService;

  @Test
  void testPublishScreenshotReviewedEventNotifiesBuyerAndMediator() {
    final ClaimReviewEventPublisher publisher =
        new ClaimReviewEventPublisher(this.mockNotificationService);
    final Claim claim = Claim.builder().id(UUID.randomUUID()).ownerId(BUYER_ID).build();
    final Campaign campaign = Campaign.builder().title("Diwali Sale").build();
    final Deal deal = Deal.builder().ownerId(MEDIATOR_ID).campaign(campaign).code("DEAL-1").build();

    publisher.publishScreenshotReviewedEvent(
        claim,
        deal,
        SCREENSHOT_TYPE_REVIEW,
        SCREENSHOT_VERIFICATION_STATUS_REJECTED,
        REVIEWER_ID,
        REVIEWER_COMMENTS);

    verify(this.mockNotificationService)
        .create(
            "Screenshot Rejected",
            "Diwali Sale (DEAL-1) - Review screenshot Rejected: " + REVIEWER_COMMENTS,
            List.of(BUYER_ID, MEDIATOR_ID),
            REVIEWER_ID);
  }
}
