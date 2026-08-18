package com.coddicted.buzzma.claim.notification;

import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimAccounting;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.service.UserService;
import com.coddicted.buzzma.notification.service.NotificationService;
import com.coddicted.buzzma.shared.util.CurrencyUtils;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PayoutEventPublisher {

  private final NotificationService notificationService;
  private final UserService userService;
  private final ClaimService claimService;

  public PayoutEventPublisher(
      final NotificationService notificationService,
      final UserService userService,
      final ClaimService claimService) {
    this.notificationService = notificationService;
    this.userService = userService;
    this.claimService = claimService;
  }

  @Async
  public void publishAgencyPaymentCapturedEvent(
      final List<ClaimAccounting> targets,
      final BigInteger totalAmountPaise,
      final UUID agencyId,
      final UUID mediatorId,
      final UUID requesterId) {
    log.info(
        "Publishing agency payment captured event for {} claims, mediator id: {}",
        targets.size(),
        mediatorId);
    final BuzzmaUser agency = userService.getById(agencyId);
    final BuzzmaUser mediator = userService.getById(mediatorId);
    final String totalAmount = CurrencyUtils.formatPaise(totalAmountPaise);

    this.notificationService.create(
        "Hurray! Payment received!",
        "Congratulations! You have received a payment of "
            + totalAmount
            + " from "
            + agency.getName()
            + " against "
            + targets.size()
            + " claims. Please visit the My Payments page for details.",
        mediatorId,
        requesterId);

    final Map<UUID, Claim> claimsById =
        this.claimService.findAllByIdAsMap(
            targets.stream().map(ClaimAccounting::getClaimId).toList());
    for (final ClaimAccounting ca : targets) {
      final Claim claim = claimsById.get(ca.getClaimId());
      final String claimAmount = CurrencyUtils.formatPaise(claim.getAmountApprovedPaise());
      this.notificationService.create(
          "Hurray! Payment for your claim released to " + mediator.getName(),
          "Congratulations! A claim amount "
              + claimAmount
              + " have received a payment of "
              + totalAmount
              + " from "
              + agency.getName()
              + " against "
              + targets.size()
              + " claims. Please visit the My Payments page for details.",
          ca.getBuyerId(),
          requesterId);
    }
  }
}
