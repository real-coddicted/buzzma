package com.coddicted.buzzma.payment.service;

import com.coddicted.buzzma.claim.model.ClaimAccountingSummary;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.payment.model.MadePayment;
import com.coddicted.buzzma.payment.model.PaidPayout;
import com.coddicted.buzzma.payment.model.PaymentReceipt;
import com.coddicted.buzzma.payment.model.PendingPayout;
import com.coddicted.buzzma.payment.model.RecordPaymentRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;

public interface PayoutService {

  List<PendingPayout> listPending(UUID callerId, UserRole role);

  List<ClaimAccountingSummary> listClaimsForPayee(UUID callerId, UUID payeeId, UserRole role);

  PaymentReceipt pay(
      UUID callerId,
      UUID payeeId,
      UserRole role,
      RecordPaymentRequest request,
      byte[] screenshotBytes,
      String screenshotFilename,
      String screenshotContentType);

  Page<PaidPayout> listPaid(UUID callerId, UserRole role, int page, int size);

  Page<MadePayment> listPayments(UUID callerId, UUID payeeId, UserRole role, int page, int size);

  Page<ClaimAccountingSummary> listClaimsForPayment(
      UUID callerId, UUID paymentId, UserRole role, int page, int size);
}
