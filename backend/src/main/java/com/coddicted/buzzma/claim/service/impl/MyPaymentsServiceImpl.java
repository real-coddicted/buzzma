package com.coddicted.buzzma.claim.service.impl;

import com.coddicted.buzzma.claim.dto.AwaitedPaymentDto;
import com.coddicted.buzzma.claim.dto.ClaimAccountingSummaryDto;
import com.coddicted.buzzma.claim.dto.PaymentReceiptDto;
import com.coddicted.buzzma.claim.dto.ReceivedPaymentDto;
import com.coddicted.buzzma.claim.entity.Payment;
import com.coddicted.buzzma.claim.persistence.ClaimAccountingRepository;
import com.coddicted.buzzma.claim.persistence.PaymentRepository;
import com.coddicted.buzzma.claim.persistence.projection.ReceivedPaymentProjection;
import com.coddicted.buzzma.claim.service.MyPaymentsService;
import com.coddicted.buzzma.identity.entity.UserRole;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MyPaymentsServiceImpl implements MyPaymentsService {

  private final ClaimAccountingRepository claimAccountingRepository;
  private final PaymentRepository paymentRepository;

  public MyPaymentsServiceImpl(
      final ClaimAccountingRepository claimAccountingRepository,
      final PaymentRepository paymentRepository) {
    this.claimAccountingRepository = claimAccountingRepository;
    this.paymentRepository = paymentRepository;
  }

  @Override
  public List<ReceivedPaymentDto> listReceived(final UUID callerId, final UserRole role) {
    final List<ReceivedPaymentProjection> projections =
        switch (role) {
          case ROLE_MEDIATOR -> claimAccountingRepository.findReceivedByMediator(callerId);
          case ROLE_BUYER -> claimAccountingRepository.findReceivedByBuyer(callerId);
          default ->
              throw new ResponseStatusException(
                  HttpStatus.FORBIDDEN, "Role not permitted for my-payments");
        };
    return toReceivedDtos(projections);
  }

  private List<ReceivedPaymentDto> toReceivedDtos(
      final List<ReceivedPaymentProjection> projections) {
    final List<UUID> ids =
        projections.stream().map(ReceivedPaymentProjection::getPaymentId).toList();
    final Map<UUID, Payment> paymentMap =
        paymentRepository.findAllById(ids).stream()
            .collect(Collectors.toMap(Payment::getId, p -> p));
    return projections.stream()
        .map(
            p -> {
              final Payment pmt = paymentMap.get(p.getPaymentId());
              return ReceivedPaymentDto.builder()
                  .paymentId(p.getPaymentId())
                  .payerId(p.getPayerId())
                  .claimCount(p.getClaimCount())
                  .totalAmountPaise(p.getTotalAmountPaise())
                  .paidAt(p.getPaidAt())
                  .paymentMethod(pmt != null ? pmt.getPaymentMethod() : null)
                  .screenshotStorageKey(pmt != null ? pmt.getScreenshotStorageKey() : null)
                  .build();
            })
        .toList();
  }

  @Override
  public List<AwaitedPaymentDto> listAwaited(final UUID callerId, final UserRole role) {
    return switch (role) {
      case ROLE_MEDIATOR ->
          claimAccountingRepository.findAwaitedByMediator(callerId).stream()
              .map(
                  p ->
                      AwaitedPaymentDto.builder()
                          .counterpartyId(p.getCounterpartyId())
                          .claimCount(p.getClaimCount())
                          .totalAmountPaise(p.getTotalAmountPaise())
                          .oldestClaimAt(p.getOldestClaimAt())
                          .build())
              .toList();
      case ROLE_BUYER ->
          claimAccountingRepository.findAwaitedByBuyer(callerId).stream()
              .map(
                  p ->
                      AwaitedPaymentDto.builder()
                          .counterpartyId(p.getCounterpartyId())
                          .claimCount(p.getClaimCount())
                          .totalAmountPaise(p.getTotalAmountPaise())
                          .oldestClaimAt(p.getOldestClaimAt())
                          .build())
              .toList();
      default ->
          throw new ResponseStatusException(
              HttpStatus.FORBIDDEN, "Role not permitted for my-payments");
    };
  }

  @Override
  public List<ClaimAccountingSummaryDto> listAwaitedClaims(
      final UUID agencyId, final UUID mediatorId) {
    return claimAccountingRepository.findClaimsForMediatorPayout(agencyId, mediatorId).stream()
        .map(
            ca ->
                ClaimAccountingSummaryDto.builder()
                    .id(ca.getId())
                    .claimId(ca.getClaimId())
                    .campaignId(ca.getCampaignId())
                    .dealId(ca.getDealId())
                    .amountPaise(ca.getMediatorReceivablePaise())
                    .createdAt(ca.getCreatedAt())
                    .build())
        .toList();
  }

  @Override
  public PaymentReceiptDto getReceipt(final UUID paymentId, final UUID callerId) {
    final Payment payment =
        paymentRepository
            .findById(paymentId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));

    if (!payment.getPayerId().equals(callerId) && !payment.getPayeeId().equals(callerId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a party to this payment");
    }

    final long claimCount =
        claimAccountingRepository.countByMediatorPaymentId(paymentId)
            + claimAccountingRepository.countByBuyerPaymentId(paymentId);

    return PaymentReceiptDto.builder()
        .id(payment.getId())
        .payerId(payment.getPayerId())
        .payeeId(payment.getPayeeId())
        .amountPaidPaise(payment.getAmountPaidPaise())
        .claimCount(claimCount)
        .paymentMethod(payment.getPaymentMethod())
        .utrRef(payment.getUtrRef())
        .notes(payment.getNotes())
        .screenshotStorageKey(payment.getScreenshotStorageKey())
        .paidAt(payment.getPaidAt())
        .build();
  }
}
