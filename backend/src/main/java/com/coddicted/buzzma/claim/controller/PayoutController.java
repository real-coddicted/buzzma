package com.coddicted.buzzma.claim.controller;

import com.coddicted.buzzma.claim.dto.ClaimAccountingSummaryDto;
import com.coddicted.buzzma.claim.dto.PaymentReceiptDto;
import com.coddicted.buzzma.claim.dto.PendingPayoutDto;
import com.coddicted.buzzma.claim.dto.RecordPaymentRequestDto;
import com.coddicted.buzzma.claim.service.PayoutService;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.shared.security.CurrentUser;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payouts")
@PreAuthorize(UserRole.Expr.AGENCY + UserRole.Expr.OR + UserRole.Expr.MEDIATOR)
public class PayoutController {

  private final PayoutService payoutService;

  public PayoutController(final PayoutService payoutService) {
    this.payoutService = payoutService;
  }

  @GetMapping("/pending")
  public List<PendingPayoutDto> listPending(@CurrentUser final BuzzmaUser currentUser) {
    return payoutService.listPending(currentUser.getId(), currentUser.getRole());
  }

  @GetMapping("/{payeeId}/claims")
  public List<ClaimAccountingSummaryDto> listClaims(
      @CurrentUser final BuzzmaUser currentUser, @PathVariable final UUID payeeId) {
    return payoutService.listClaimsForPayee(currentUser.getId(), payeeId, currentUser.getRole());
  }

  @PostMapping("/{payeeId}/pay")
  @ResponseStatus(HttpStatus.CREATED)
  public PaymentReceiptDto pay(
      @CurrentUser final BuzzmaUser currentUser,
      @PathVariable final UUID payeeId,
      @Valid @RequestBody final RecordPaymentRequestDto request) {
    return payoutService.pay(currentUser.getId(), payeeId, currentUser.getRole(), request);
  }
}
