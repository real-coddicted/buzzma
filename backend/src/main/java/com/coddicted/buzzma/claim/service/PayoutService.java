package com.coddicted.buzzma.claim.service;

import com.coddicted.buzzma.claim.dto.ClaimAccountingSummaryDto;
import com.coddicted.buzzma.claim.dto.PaymentReceiptDto;
import com.coddicted.buzzma.claim.dto.PendingPayoutDto;
import com.coddicted.buzzma.claim.dto.RecordPaymentRequestDto;
import com.coddicted.buzzma.identity.entity.UserRole;
import java.util.List;
import java.util.UUID;

public interface PayoutService {

  List<PendingPayoutDto> listPending(UUID callerId, UserRole role);

  List<ClaimAccountingSummaryDto> listClaimsForPayee(UUID callerId, UUID payeeId, UserRole role);

  PaymentReceiptDto pay(
      UUID callerId, UUID payeeId, UserRole role, RecordPaymentRequestDto request);
}
