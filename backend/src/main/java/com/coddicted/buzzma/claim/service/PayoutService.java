package com.coddicted.buzzma.claim.service;

import com.coddicted.buzzma.claim.model.ClaimAccountingSummary;
import com.coddicted.buzzma.claim.model.MadePayment;
import com.coddicted.buzzma.claim.model.PaidPayout;
import com.coddicted.buzzma.claim.model.PaymentReceipt;
import com.coddicted.buzzma.claim.model.PendingPayout;
import com.coddicted.buzzma.claim.model.RecordPaymentRequest;
import com.coddicted.buzzma.identity.entity.UserRole;
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
