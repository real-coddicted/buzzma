package com.coddicted.buzzma.claim.service;

import com.coddicted.buzzma.claim.dto.AwaitedPaymentDto;
import com.coddicted.buzzma.claim.dto.ClaimAccountingSummaryDto;
import com.coddicted.buzzma.claim.dto.PaymentReceiptDto;
import com.coddicted.buzzma.claim.dto.ReceivedPaymentDto;
import com.coddicted.buzzma.identity.entity.UserRole;
import java.util.List;
import java.util.UUID;

public interface MyPaymentsService {

  List<ReceivedPaymentDto> listReceived(UUID callerId, UserRole role);

  List<AwaitedPaymentDto> listAwaited(UUID callerId, UserRole role);

  List<ClaimAccountingSummaryDto> listAwaitedClaims(UUID agencyId, UUID mediatorId);

  PaymentReceiptDto getReceipt(UUID paymentId, UUID callerId);
}
