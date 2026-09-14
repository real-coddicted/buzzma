package com.coddicted.buzzma.payment.service;

import com.coddicted.buzzma.claim.model.ClaimAccountingSummary;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.payment.model.AwaitedPayment;
import com.coddicted.buzzma.payment.model.PaymentReceipt;
import com.coddicted.buzzma.payment.model.ReceivedPayment;
import java.util.List;
import java.util.UUID;

public interface MyPaymentsService {

  List<ReceivedPayment> listReceived(UUID callerId, UserRole role);

  List<AwaitedPayment> listAwaited(UUID callerId, UserRole role);

  List<ClaimAccountingSummary> listAwaitedClaims(UUID counterpartyId, UUID callerId, UserRole role);

  List<ClaimAccountingSummary> listReceivedClaims(UUID paymentId, UUID callerId, UserRole role);

  PaymentReceipt getReceipt(UUID paymentId, UUID callerId);
}
