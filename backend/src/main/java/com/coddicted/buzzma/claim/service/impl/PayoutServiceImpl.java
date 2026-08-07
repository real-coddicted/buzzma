package com.coddicted.buzzma.claim.service.impl;

import com.coddicted.buzzma.claim.entity.AccountingPaymentStatus;
import com.coddicted.buzzma.claim.entity.ClaimAccounting;
import com.coddicted.buzzma.claim.entity.Payment;
import com.coddicted.buzzma.claim.mapper.PaymentMapper;
import com.coddicted.buzzma.claim.model.ClaimAccountingSummary;
import com.coddicted.buzzma.claim.model.PaymentReceipt;
import com.coddicted.buzzma.claim.model.PendingPayout;
import com.coddicted.buzzma.claim.model.RecordPaymentRequest;
import com.coddicted.buzzma.claim.persistence.ClaimAccountingRepository;
import com.coddicted.buzzma.claim.persistence.PaymentRepository;
import com.coddicted.buzzma.claim.service.PayoutService;
import com.coddicted.buzzma.identity.entity.UserRole;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PayoutServiceImpl implements PayoutService {

  private final ClaimAccountingRepository claimAccountingRepository;
  private final PaymentRepository paymentRepository;
  private final PaymentMapper paymentMapper;

  public PayoutServiceImpl(
      final ClaimAccountingRepository claimAccountingRepository,
      final PaymentRepository paymentRepository,
      final PaymentMapper paymentMapper) {
    this.claimAccountingRepository = claimAccountingRepository;
    this.paymentRepository = paymentRepository;
    this.paymentMapper = paymentMapper;
  }

  @Override
  public List<PendingPayout> listPending(final UUID callerId, final UserRole role) {
    return switch (role) {
      case ROLE_AGENCY ->
          paymentMapper.toPendingPayouts(claimAccountingRepository.findPendingByAgency(callerId));
      case ROLE_MEDIATOR ->
          paymentMapper.toPendingPayouts(claimAccountingRepository.findPendingByMediator(callerId));
      default ->
          throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Role not permitted for payouts");
    };
  }

  @Override
  public List<ClaimAccountingSummary> listClaimsForPayee(
      final UUID callerId, final UUID payeeId, final UserRole role) {
    return switch (role) {
      case ROLE_AGENCY ->
          claimAccountingRepository.findClaimsForMediatorPayout(callerId, payeeId).stream()
              .map(paymentMapper::toSummaryForAgency)
              .toList();
      case ROLE_MEDIATOR ->
          claimAccountingRepository.findClaimsForBuyerPayout(callerId, payeeId).stream()
              .map(paymentMapper::toSummaryForMediator)
              .toList();
      default ->
          throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Role not permitted for payouts");
    };
  }

  @Override
  @Transactional
  public PaymentReceipt pay(
      final UUID callerId,
      final UUID payeeId,
      final UserRole role,
      final RecordPaymentRequest request) {
    final List<ClaimAccounting> targets = resolveAndLockTargets(callerId, payeeId, role, request);
    if (targets.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No eligible pending claims found");
    }
    validateAllPending(targets, role);

    final BigInteger totalAmount = sumAmount(targets, role);
    final Instant now = Instant.now();

    final Payment payment =
        Payment.builder()
            .payerId(callerId)
            .payeeId(payeeId)
            .amountPaidPaise(totalAmount)
            .paymentMethod(request.getPaymentMethod())
            .utrRef(request.getUtrRef())
            .notes(request.getNotes())
            .paidAt(request.getPaidAt())
            .createdBy(callerId)
            .updatedBy(callerId)
            .build();
    final Payment saved = paymentRepository.saveAndFlush(payment);

    final List<UUID> ids = targets.stream().map(ClaimAccounting::getId).toList();
    if (role == UserRole.ROLE_AGENCY) {
      claimAccountingRepository.markMediatorPaid(
          ids, saved.getId(), request.getPaidAt(), now, callerId);
    } else {
      claimAccountingRepository.markBuyerPaid(
          ids, saved.getId(), request.getPaidAt(), now, callerId);
    }

    return paymentMapper.toReceipt(saved, targets.size());
  }

  private List<ClaimAccounting> resolveAndLockTargets(
      final UUID callerId,
      final UUID payeeId,
      final UserRole role,
      final RecordPaymentRequest request) {
    final boolean isPartial = request.getClaimIds() != null && !request.getClaimIds().isEmpty();
    if (isPartial) {
      final List<ClaimAccounting> locked =
          claimAccountingRepository.findByIdInForUpdate(request.getClaimIds());
      validateOwnership(locked, callerId, payeeId, role, request.getClaimIds().size());
      return locked;
    }
    return switch (role) {
      case ROLE_AGENCY ->
          claimAccountingRepository.findPendingByAgencyAndMediatorForUpdate(callerId, payeeId);
      case ROLE_MEDIATOR ->
          claimAccountingRepository.findPendingByMediatorAndBuyerForUpdate(callerId, payeeId);
      default ->
          throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Role not permitted for payouts");
    };
  }

  private void validateOwnership(
      final List<ClaimAccounting> rows,
      final UUID callerId,
      final UUID payeeId,
      final UserRole role,
      final int requestedCount) {
    if (rows.size() != requestedCount) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "One or more supplied claimIds not found");
    }
    for (final ClaimAccounting ca : rows) {
      final boolean owned =
          role == UserRole.ROLE_AGENCY
              ? ca.getAgencyId().equals(callerId) && ca.getMediatorId().equals(payeeId)
              : ca.getMediatorId().equals(callerId) && ca.getBuyerId().equals(payeeId);
      if (!owned) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "claimId " + ca.getId() + " does not belong to this payout");
      }
    }
  }

  private void validateAllPending(final List<ClaimAccounting> rows, final UserRole role) {
    for (final ClaimAccounting ca : rows) {
      final AccountingPaymentStatus status =
          role == UserRole.ROLE_AGENCY ? ca.getMediatorPaymentStatus() : ca.getBuyerPaymentStatus();
      if (status != AccountingPaymentStatus.PENDING) {
        throw new ResponseStatusException(
            HttpStatus.CONFLICT, "Claim " + ca.getId() + " is already paid");
      }
    }
  }

  private BigInteger sumAmount(final List<ClaimAccounting> rows, final UserRole role) {
    return rows.stream()
        .map(
            ca ->
                role == UserRole.ROLE_AGENCY
                    ? ca.getMediatorReceivablePaise()
                    : ca.getBuyerReceivablePaise())
        .reduce(BigInteger.ZERO, BigInteger::add);
  }
}
