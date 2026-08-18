package com.coddicted.buzzma.claim.notification;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimAccounting;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.service.UserService;
import com.coddicted.buzzma.notification.service.NotificationService;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PayoutEventPublisherTest {

  private static final UUID AGENCY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID MEDIATOR_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID BUYER_ONE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
  private static final UUID BUYER_TWO_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
  private static final UUID CLAIM_ONE_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
  private static final UUID CLAIM_TWO_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");

  @Mock private NotificationService mockNotificationService;
  @Mock private UserService mockUserService;
  @Mock private ClaimService mockClaimService;

  @Test
  void testPublishAgencyPaymentCapturedEventNotifiesMediatorAndEachBuyer() {
    final PayoutEventPublisher publisher =
        new PayoutEventPublisher(mockNotificationService, mockUserService, mockClaimService);

    final ClaimAccounting caOne =
        ClaimAccounting.builder()
            .id(UUID.randomUUID())
            .claimId(CLAIM_ONE_ID)
            .buyerId(BUYER_ONE_ID)
            .build();
    final ClaimAccounting caTwo =
        ClaimAccounting.builder()
            .id(UUID.randomUUID())
            .claimId(CLAIM_TWO_ID)
            .buyerId(BUYER_TWO_ID)
            .build();
    final List<ClaimAccounting> targets = List.of(caOne, caTwo);

    when(mockUserService.getById(AGENCY_ID))
        .thenReturn(BuzzmaUser.builder().id(AGENCY_ID).name("Acme Agency").build());
    when(mockUserService.getById(MEDIATOR_ID))
        .thenReturn(BuzzmaUser.builder().id(MEDIATOR_ID).name("Mira Mediator").build());
    when(mockClaimService.findAllByIdAsMap(List.of(CLAIM_ONE_ID, CLAIM_TWO_ID)))
        .thenReturn(
            Map.of(
                CLAIM_ONE_ID,
                Claim.builder()
                    .id(CLAIM_ONE_ID)
                    .amountApprovedPaise(BigInteger.valueOf(5000))
                    .build(),
                CLAIM_TWO_ID,
                Claim.builder()
                    .id(CLAIM_TWO_ID)
                    .amountApprovedPaise(BigInteger.valueOf(3000))
                    .build()));

    publisher.publishAgencyPaymentCapturedEvent(
        targets, BigInteger.valueOf(8000), AGENCY_ID, MEDIATOR_ID, AGENCY_ID);

    verify(mockNotificationService)
        .create(
            "Hurray! Payment received!",
            "Congratulations! You have received a payment of ₹80.00 from Acme Agency against 2"
                + " claims. Please visit the My Payments page for details.",
            MEDIATOR_ID,
            AGENCY_ID);
    verify(mockNotificationService)
        .create(
            "Hurray! Payment for your claim released to Mira Mediator",
            "Congratulations! A claim amount ₹50.00 have received a payment of ₹80.00 from Acme"
                + " Agency against 2 claims. Please visit the My Payments page for details.",
            BUYER_ONE_ID,
            AGENCY_ID);
    verify(mockNotificationService)
        .create(
            "Hurray! Payment for your claim released to Mira Mediator",
            "Congratulations! A claim amount ₹30.00 have received a payment of ₹80.00 from Acme"
                + " Agency against 2 claims. Please visit the My Payments page for details.",
            BUYER_TWO_ID,
            AGENCY_ID);
  }
}
