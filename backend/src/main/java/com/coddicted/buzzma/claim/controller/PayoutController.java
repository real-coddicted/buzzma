package com.coddicted.buzzma.claim.controller;

import com.coddicted.buzzma.claim.dto.ClaimAccountingSummaryDto;
import com.coddicted.buzzma.claim.dto.PaymentReceiptDto;
import com.coddicted.buzzma.claim.dto.PendingPayoutDto;
import com.coddicted.buzzma.claim.dto.RecordPaymentRequestDto;
import com.coddicted.buzzma.claim.mapper.PaymentMapper;
import com.coddicted.buzzma.claim.service.PayoutService;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.shared.security.CurrentUser;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

// For User-payout page
@RestController
@RequestMapping("/api/v1/payouts")
@PreAuthorize(UserRole.Expr.AGENCY + UserRole.Expr.OR + UserRole.Expr.MEDIATOR)
public class PayoutController {

  private final PayoutService payoutService;
  private final PaymentMapper paymentMapper;

  public PayoutController(final PayoutService payoutService, final PaymentMapper paymentMapper) {
    this.payoutService = payoutService;
    this.paymentMapper = paymentMapper;
  }

  @GetMapping("/pending")
  public List<PendingPayoutDto> listPending(@CurrentUser final BuzzmaUser currentUser) {
    return payoutService.listPending(currentUser.getId(), currentUser.getRole()).stream()
        .map(paymentMapper::toDto)
        .toList();
  }

  @GetMapping("/{payeeId}/claims")
  public List<ClaimAccountingSummaryDto> listClaims(
      @CurrentUser final BuzzmaUser currentUser, @PathVariable final UUID payeeId) {
    return payoutService
        .listClaimsForPayee(currentUser.getId(), payeeId, currentUser.getRole())
        .stream()
        .map(paymentMapper::toDto)
        .toList();
  }

  @PostMapping(value = "/{payeeId}/pay", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public PaymentReceiptDto pay(
      @CurrentUser final BuzzmaUser currentUser,
      @PathVariable final UUID payeeId,
      @RequestPart("request") @Valid final RecordPaymentRequestDto request,
      @RequestPart("screenshot") final MultipartFile screenshot) {
    try {
      return paymentMapper.toDto(
          payoutService.pay(
              currentUser.getId(),
              payeeId,
              currentUser.getRole(),
              paymentMapper.toModel(request),
              screenshot.getBytes(),
              screenshot.getOriginalFilename(),
              screenshot.getContentType()));
    } catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to read screenshot file");
    }
  }
}
