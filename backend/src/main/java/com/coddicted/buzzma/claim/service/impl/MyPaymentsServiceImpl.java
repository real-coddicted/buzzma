package com.coddicted.buzzma.claim.service.impl;

import com.coddicted.buzzma.claim.entity.Payment;
import com.coddicted.buzzma.claim.mapper.PaymentMapper;
import com.coddicted.buzzma.claim.model.AwaitedPayment;
import com.coddicted.buzzma.claim.model.ClaimAccountingSummary;
import com.coddicted.buzzma.claim.model.PaymentReceipt;
import com.coddicted.buzzma.claim.model.ReceivedPayment;
import com.coddicted.buzzma.claim.persistence.projection.ReceivedPaymentProjection;
import com.coddicted.buzzma.claim.service.ClaimAccountingService;
import com.coddicted.buzzma.claim.service.MyPaymentsService;
import com.coddicted.buzzma.claim.service.PaymentService;
import com.coddicted.buzzma.identity.entity.UserRole;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MyPaymentsServiceImpl implements MyPaymentsService {

  private final ClaimAccountingService claimAccountingService;
  private final PaymentService paymentService;
  private final PaymentMapper paymentMapper;

  public MyPaymentsServiceImpl(
      final ClaimAccountingService claimAccountingService,
      final PaymentService paymentService,
      final PaymentMapper paymentMapper) {
    this.claimAccountingService = claimAccountingService;
    this.paymentService = paymentService;
    this.paymentMapper = paymentMapper;
  }

  @Override
  public List<ReceivedPayment> listReceived(final UUID callerId, final UserRole role) {
    final List<ReceivedPaymentProjection> projections =
        switch (role) {
          case ROLE_MEDIATOR -> claimAccountingService.findReceivedByMediator(callerId);
          case ROLE_BUYER -> claimAccountingService.findReceivedByBuyer(callerId);
          default ->
              throw new ResponseStatusException(
                  HttpStatus.FORBIDDEN, "Role not permitted for my-payments");
        };
    final Map<UUID, Payment> paymentMap =
        paymentService.findAllByIdAsMap(
            projections.stream().map(ReceivedPaymentProjection::getPaymentId).toList());
    return projections.stream()
        .map(p -> paymentMapper.toReceivedPayment(p, paymentMap.get(p.getPaymentId())))
        .toList();
  }

  @Override
  public List<AwaitedPayment> listAwaited(final UUID callerId, final UserRole role) {
    return switch (role) {
      case ROLE_MEDIATOR ->
          paymentMapper.toAwaitedPayments(claimAccountingService.findAwaitedByMediator(callerId));
      case ROLE_BUYER ->
          paymentMapper.toAwaitedPayments(claimAccountingService.findAwaitedByBuyer(callerId));
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
          claimAccountingService
              .findClaimsPendingForMediatorPayout(counterpartyId, callerId)
              .stream()
              .map(paymentMapper::toSummaryForAgency)
              .toList();
      case ROLE_BUYER ->
          claimAccountingService.findClaimsPendingForBuyerPayout(counterpartyId, callerId).stream()
              .map(paymentMapper::toSummaryForMediator)
              .toList();
      default ->
          throw new ResponseStatusException(
              HttpStatus.FORBIDDEN, "Role not permitted for my-payments");
    };
  }

  @Override
  public List<ClaimAccountingSummary> listReceivedClaims(
      final UUID paymentId, final UUID callerId, final UserRole role) {
    return switch (role) {
      case ROLE_MEDIATOR ->
          claimAccountingService.findClaimsByMediatorPaymentId(callerId, paymentId).stream()
              .map(paymentMapper::toSummaryForAgency)
              .toList();
      case ROLE_BUYER ->
          claimAccountingService.findClaimsByBuyerPaymentId(callerId, paymentId).stream()
              .map(paymentMapper::toSummaryForMediator)
              .toList();
      default ->
          throw new ResponseStatusException(
              HttpStatus.FORBIDDEN, "Role not permitted for my-payments");
    };
  }

  @Override
  public PaymentReceipt getReceipt(final UUID paymentId, final UUID callerId) {
    final Payment payment = paymentService.getById(paymentId);

    if (!payment.getPayerId().equals(callerId) && !payment.getPayeeId().equals(callerId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a party to this payment");
    }

    final long claimCount =
        claimAccountingService.countByMediatorPaymentId(paymentId)
            + claimAccountingService.countByBuyerPaymentId(paymentId);

    return paymentMapper.toReceipt(payment, claimCount);
  }
}
