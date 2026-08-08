package com.coddicted.buzzma.claim.service;

import com.coddicted.buzzma.claim.model.AwaitedPayment;
import com.coddicted.buzzma.claim.model.ClaimAccountingSummary;
import com.coddicted.buzzma.claim.model.PaymentReceipt;
import com.coddicted.buzzma.claim.model.ReceivedPayment;
import com.coddicted.buzzma.identity.entity.UserRole;
import java.util.List;
import java.util.UUID;

public interface MyPaymentsService {

  List<ReceivedPayment> listReceived(UUID callerId, UserRole role);

  List<AwaitedPayment> listAwaited(UUID callerId, UserRole role);

  List<ClaimAccountingSummary> listAwaitedClaims(UUID counterpartyId, UUID callerId, UserRole role);

  List<ClaimAccountingSummary> listReceivedClaims(UUID paymentId, UUID callerId, UserRole role);

  PaymentReceipt getReceipt(UUID paymentId, UUID callerId);
}
