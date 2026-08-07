package com.coddicted.buzzma.claim.controller;

import com.coddicted.buzzma.claim.dto.AwaitedPaymentDto;
import com.coddicted.buzzma.claim.dto.ClaimAccountingSummaryDto;
import com.coddicted.buzzma.claim.dto.ReceivedPaymentDto;
import com.coddicted.buzzma.claim.mapper.PaymentMapper;
import com.coddicted.buzzma.claim.service.MyPaymentsService;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.shared.security.CurrentUser;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// For My-payments page
@RestController
@RequestMapping("/api/v1/my-payments")
@PreAuthorize(UserRole.Expr.MEDIATOR + UserRole.Expr.OR + UserRole.Expr.BUYER)
public class MyPaymentsController {

  private final MyPaymentsService myPaymentsService;
  private final PaymentMapper paymentMapper;

  public MyPaymentsController(
      final MyPaymentsService myPaymentsService, final PaymentMapper paymentMapper) {
    this.myPaymentsService = myPaymentsService;
    this.paymentMapper = paymentMapper;
  }

  // Role-dispatch: mediator sees hop-1 PAID grouped by payment batch; buyer sees hop-2 PAID.
  @GetMapping("/received")
  public List<ReceivedPaymentDto> listReceived(@CurrentUser final BuzzmaUser currentUser) {
    return myPaymentsService.listReceived(currentUser.getId(), currentUser.getRole()).stream()
        .map(paymentMapper::toDto)
        .toList();
  }

  // Role-dispatch: mediator sees PENDING hop-1 grouped by agency; buyer sees PENDING hop-2 grouped
  // by mediator.
  @GetMapping("/awaited")
  public List<AwaitedPaymentDto> listAwaited(@CurrentUser final BuzzmaUser currentUser) {
    return myPaymentsService.listAwaited(currentUser.getId(), currentUser.getRole()).stream()
        .map(paymentMapper::toDto)
        .toList();
  }

  @GetMapping("/awaited/{counterpartyId}/claims")
  public List<ClaimAccountingSummaryDto> listAwaitedClaims(
      @CurrentUser final BuzzmaUser currentUser, @PathVariable final UUID counterpartyId) {
    return myPaymentsService
        .listAwaitedClaims(counterpartyId, currentUser.getId(), currentUser.getRole())
        .stream()
        .map(paymentMapper::toDto)
        .toList();
  }
}
