package com.coddicted.buzzma.claim.service.impl;

import com.coddicted.buzzma.claim.entity.AccountingPaymentStatus;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimAccounting;
import com.coddicted.buzzma.claim.entity.Payment;
import com.coddicted.buzzma.claim.mapper.PaymentMapper;
import com.coddicted.buzzma.claim.model.ClaimAccountingSummary;
import com.coddicted.buzzma.claim.model.MadePayment;
import com.coddicted.buzzma.claim.model.PaidPayout;
import com.coddicted.buzzma.claim.model.PaymentReceipt;
import com.coddicted.buzzma.claim.model.PendingPayout;
import com.coddicted.buzzma.claim.model.RecordPaymentRequest;
import com.coddicted.buzzma.claim.persistence.projection.MadePaymentProjection;
import com.coddicted.buzzma.claim.service.ClaimAccountingService;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.claim.service.PaymentService;
import com.coddicted.buzzma.claim.service.PayoutService;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.storage.service.StorageService;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PayoutServiceImpl implements PayoutService {

  private final ClaimAccountingService claimAccountingService;
  private final PaymentService paymentService;
  private final ClaimService claimService;
  private final PaymentMapper paymentMapper;
  private final StorageService storageService;

  public PayoutServiceImpl(
      final ClaimAccountingService claimAccountingService,
      final PaymentService paymentService,
      final ClaimService claimService,
      final PaymentMapper paymentMapper,
      final StorageService storageService) {
    this.claimAccountingService = claimAccountingService;
    this.paymentService = paymentService;
    this.claimService = claimService;
    this.paymentMapper = paymentMapper;
    this.storageService = storageService;
  }

  @Override
  public List<PendingPayout> listPending(final UUID callerId, final UserRole role) {
    return switch (role) {
      case ROLE_AGENCY ->
          paymentMapper.toPendingPayouts(claimAccountingService.findPendingByAgency(callerId));
      case ROLE_MEDIATOR ->
          paymentMapper.toPendingPayouts(claimAccountingService.findPendingByMediator(callerId));
      default ->
          throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Role not permitted for payouts");
    };
  }

  @Override
  public List<ClaimAccountingSummary> listClaimsForPayee(
      final UUID callerId, final UUID payeeId, final UserRole role) {
    final List<ClaimAccounting> rows =
        switch (role) {
          case ROLE_AGENCY ->
              claimAccountingService.findClaimsPendingForMediatorPayout(callerId, payeeId);
          case ROLE_MEDIATOR ->
              claimAccountingService.findClaimsPendingForBuyerPayout(callerId, payeeId);
          default ->
              throw new ResponseStatusException(
                  HttpStatus.FORBIDDEN, "Role not permitted for payouts");
        };
    final Map<UUID, Claim> claimsById = findClaimsFor(rows);
    return role == UserRole.ROLE_AGENCY
        ? rows.stream()
            .map(ca -> paymentMapper.toSummaryForAgency(ca, claimsById.get(ca.getClaimId())))
            .toList()
        : rows.stream()
            .map(ca -> paymentMapper.toSummaryForMediator(ca, claimsById.get(ca.getClaimId())))
            .toList();
  }

  private Map<UUID, Claim> findClaimsFor(final List<ClaimAccounting> rows) {
    return claimService.findAllByIdAsMap(rows.stream().map(ClaimAccounting::getClaimId).toList());
  }

  @Override
  @Transactional
  public PaymentReceipt pay(
      final UUID callerId,
      final UUID payeeId,
      final UserRole role,
      final RecordPaymentRequest request,
      final byte[] screenshotBytes,
      final String screenshotFilename,
      final String screenshotContentType) {
    final List<ClaimAccounting> targets = resolveAndLockTargets(callerId, payeeId, role, request);
    if (targets.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No eligible pending claims found");
    }
    validateAllPending(targets, role);

    final BigInteger totalAmount = sumAmount(targets, role);
    final Instant now = Instant.now();

    final String screenshotKey =
        storageService.store(
            "payments", screenshotFilename, screenshotContentType, screenshotBytes);

    final Payment payment =
        Payment.builder()
            .payerId(callerId)
            .payeeId(payeeId)
            .screenshotStorageKey(screenshotKey)
            .amountPaidPaise(totalAmount)
            .paymentMethod(request.getPaymentMethod())
            .utrRef(request.getUtrRef())
            .notes(request.getNotes())
            .paidAt(request.getPaidAt())
            .createdBy(callerId)
            .updatedBy(callerId)
            .build();
    final Payment saved = paymentService.save(payment);

    final List<UUID> ids = targets.stream().map(ClaimAccounting::getId).toList();
    if (role == UserRole.ROLE_AGENCY) {
      claimAccountingService.markMediatorPaid(
          ids, saved.getId(), request.getPaidAt(), now, callerId);
    } else {
      claimAccountingService.markBuyerPaid(ids, saved.getId(), request.getPaidAt(), now, callerId);
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
          claimAccountingService.findByIdInForUpdate(request.getClaimIds());
      validateOwnership(locked, callerId, payeeId, role, request.getClaimIds().size());
      return locked;
    }
    return switch (role) {
      case ROLE_AGENCY ->
          claimAccountingService.findPendingByAgencyAndMediatorForUpdate(callerId, payeeId);
      case ROLE_MEDIATOR ->
          claimAccountingService.findPendingByMediatorAndBuyerForUpdate(callerId, payeeId);
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

  @Override
  public Page<PaidPayout> listPaid(
      final UUID callerId, final UserRole role, final int page, final int size) {
    final PageRequest pageRequest = PageRequest.of(page, size);
    return switch (role) {
      case ROLE_AGENCY ->
          claimAccountingService
              .findPaidByAgency(callerId, pageRequest)
              .map(paymentMapper::toPaidPayout);
      case ROLE_MEDIATOR ->
          claimAccountingService
              .findPaidByMediator(callerId, pageRequest)
              .map(paymentMapper::toPaidPayout);
      default ->
          throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Role not permitted for payouts");
    };
  }

  @Override
  public Page<MadePayment> listPayments(
      final UUID callerId,
      final UUID payeeId,
      final UserRole role,
      final int page,
      final int size) {
    final PageRequest pageRequest = PageRequest.of(page, size);
    final Page<MadePaymentProjection> projections =
        switch (role) {
          case ROLE_AGENCY ->
              claimAccountingService.findPaymentsPaidToMediator(callerId, payeeId, pageRequest);
          case ROLE_MEDIATOR ->
              claimAccountingService.findPaymentsPaidToBuyer(callerId, payeeId, pageRequest);
          default ->
              throw new ResponseStatusException(
                  HttpStatus.FORBIDDEN, "Role not permitted for payouts");
        };
    final Map<UUID, Payment> paymentMap =
        paymentService.findAllByIdAsMap(
            projections.getContent().stream().map(MadePaymentProjection::getPaymentId).toList());
    return projections.map(p -> paymentMapper.toMadePayment(p, paymentMap.get(p.getPaymentId())));
  }

  @Override
  public Page<ClaimAccountingSummary> listClaimsForPayment(
      final UUID callerId,
      final UUID paymentId,
      final UserRole role,
      final int page,
      final int size) {
    final PageRequest pageRequest = PageRequest.of(page, size);
    final Page<ClaimAccounting> rows =
        switch (role) {
          case ROLE_AGENCY ->
              claimAccountingService.findClaimsPaidToMediatorByPayment(
                  callerId, paymentId, pageRequest);
          case ROLE_MEDIATOR ->
              claimAccountingService.findClaimsPaidToBuyerByPayment(
                  callerId, paymentId, pageRequest);
          default ->
              throw new ResponseStatusException(
                  HttpStatus.FORBIDDEN, "Role not permitted for payouts");
        };
    final Map<UUID, Claim> claimsById =
        claimService.findAllByIdAsMap(
            rows.getContent().stream().map(ClaimAccounting::getClaimId).toList());
    return role == UserRole.ROLE_AGENCY
        ? rows.map(ca -> paymentMapper.toSummaryForAgency(ca, claimsById.get(ca.getClaimId())))
        : rows.map(ca -> paymentMapper.toSummaryForMediator(ca, claimsById.get(ca.getClaimId())));
  }
}
