package com.coddicted.buzzma.claim.service.impl;

import com.coddicted.buzzma.claim.entity.Payment;
import com.coddicted.buzzma.claim.mapper.PaymentMapper;
import com.coddicted.buzzma.claim.model.AwaitedPayment;
import com.coddicted.buzzma.claim.model.ClaimAccountingSummary;
import com.coddicted.buzzma.claim.model.PaymentReceipt;
import com.coddicted.buzzma.claim.model.ReceivedPayment;
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
  private final PaymentMapper paymentMapper;

  public MyPaymentsServiceImpl(
      final ClaimAccountingRepository claimAccountingRepository,
      final PaymentRepository paymentRepository,
      final PaymentMapper paymentMapper) {
    this.claimAccountingRepository = claimAccountingRepository;
    this.paymentRepository = paymentRepository;
    this.paymentMapper = paymentMapper;
  }

  @Override
  public List<ReceivedPayment> listReceived(final UUID callerId, final UserRole role) {
    final List<ReceivedPaymentProjection> projections =
        switch (role) {
          case ROLE_MEDIATOR -> claimAccountingRepository.findReceivedByMediator(callerId);
          case ROLE_BUYER -> claimAccountingRepository.findReceivedByBuyer(callerId);
          default ->
              throw new ResponseStatusException(
                  HttpStatus.FORBIDDEN, "Role not permitted for my-payments");
        };
    final Map<UUID, Payment> paymentMap =
        paymentRepository
            .findAllById(projections.stream().map(ReceivedPaymentProjection::getPaymentId).toList())
            .stream()
            .collect(Collectors.toMap(Payment::getId, p -> p));
    return projections.stream()
        .map(p -> paymentMapper.toReceivedPayment(p, paymentMap.get(p.getPaymentId())))
        .toList();
  }

  @Override
  public List<AwaitedPayment> listAwaited(final UUID callerId, final UserRole role) {
    return switch (role) {
      case ROLE_MEDIATOR ->
          paymentMapper.toAwaitedPayments(
              claimAccountingRepository.findAwaitedByMediator(callerId));
      case ROLE_BUYER ->
          paymentMapper.toAwaitedPayments(claimAccountingRepository.findAwaitedByBuyer(callerId));
      default ->
          throw new ResponseStatusException(
              HttpStatus.FORBIDDEN, "Role not permitted for my-payments");
    };
  }

  @Override
  public List<ClaimAccountingSummary> listAwaitedClaims(
      final UUID counterpartyId, final UUID callerId, final UserRole role) {
    return switch (role) {
      case ROLE_MEDIATOR ->
          claimAccountingRepository
              .findClaimsPendingForMediatorPayout(counterpartyId, callerId)
              .stream()
              .map(paymentMapper::toSummaryForAgency)
              .toList();
      case ROLE_BUYER ->
          claimAccountingRepository
              .findClaimsPendingForBuyerPayout(counterpartyId, callerId)
              .stream()
              .map(paymentMapper::toSummaryForMediator)
              .toList();
      default ->
          throw new ResponseStatusException(
              HttpStatus.FORBIDDEN, "Role not permitted for my-payments");
    };
  }

  @Override
  public PaymentReceipt getReceipt(final UUID paymentId, final UUID callerId) {
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

    return paymentMapper.toReceipt(payment, claimCount);
  }
}
