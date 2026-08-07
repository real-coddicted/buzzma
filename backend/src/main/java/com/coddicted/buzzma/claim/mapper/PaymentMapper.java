package com.coddicted.buzzma.claim.mapper;

import com.coddicted.buzzma.claim.dto.AwaitedPaymentDto;
import com.coddicted.buzzma.claim.dto.ClaimAccountingSummaryDto;
import com.coddicted.buzzma.claim.dto.PaymentReceiptDto;
import com.coddicted.buzzma.claim.dto.PendingPayoutDto;
import com.coddicted.buzzma.claim.dto.ReceivedPaymentDto;
import com.coddicted.buzzma.claim.dto.RecordPaymentRequestDto;
import com.coddicted.buzzma.claim.entity.ClaimAccounting;
import com.coddicted.buzzma.claim.entity.Payment;
import com.coddicted.buzzma.claim.model.AwaitedPayment;
import com.coddicted.buzzma.claim.model.ClaimAccountingSummary;
import com.coddicted.buzzma.claim.model.PaymentReceipt;
import com.coddicted.buzzma.claim.model.PendingPayout;
import com.coddicted.buzzma.claim.model.ReceivedPayment;
import com.coddicted.buzzma.claim.model.RecordPaymentRequest;
import com.coddicted.buzzma.claim.persistence.projection.AwaitedPaymentProjection;
import com.coddicted.buzzma.claim.persistence.projection.PendingPayoutProjection;
import com.coddicted.buzzma.claim.persistence.projection.ReceivedPaymentProjection;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PaymentMapper {

  // ── Projection / entity → model (service layer) ─────────────────────────────────────────────

  AwaitedPayment toAwaitedPayment(AwaitedPaymentProjection projection);

  List<AwaitedPayment> toAwaitedPayments(List<AwaitedPaymentProjection> projections);

  PendingPayout toPendingPayout(PendingPayoutProjection projection);

  List<PendingPayout> toPendingPayouts(List<PendingPayoutProjection> projections);

  @Mapping(source = "projection.paymentId", target = "paymentId")
  @Mapping(source = "projection.payerId", target = "payerId")
  @Mapping(source = "projection.claimCount", target = "claimCount")
  @Mapping(source = "projection.totalAmountPaise", target = "totalAmountPaise")
  @Mapping(source = "projection.paidAt", target = "paidAt")
  @Mapping(source = "payment.paymentMethod", target = "paymentMethod")
  @Mapping(source = "payment.screenshotStorageKey", target = "screenshotStorageKey")
  ReceivedPayment toReceivedPayment(ReceivedPaymentProjection projection, Payment payment);

  @Mapping(source = "mediatorReceivablePaise", target = "amountPaise")
  ClaimAccountingSummary toSummaryForAgency(ClaimAccounting ca);

  @Mapping(source = "buyerReceivablePaise", target = "amountPaise")
  ClaimAccountingSummary toSummaryForMediator(ClaimAccounting ca);

  @Mapping(source = "payment.id", target = "id")
  @Mapping(source = "payment.payerId", target = "payerId")
  @Mapping(source = "payment.payeeId", target = "payeeId")
  @Mapping(source = "payment.amountPaidPaise", target = "amountPaidPaise")
  @Mapping(source = "payment.paymentMethod", target = "paymentMethod")
  @Mapping(source = "payment.utrRef", target = "utrRef")
  @Mapping(source = "payment.notes", target = "notes")
  @Mapping(source = "payment.screenshotStorageKey", target = "screenshotStorageKey")
  @Mapping(source = "payment.paidAt", target = "paidAt")
  @Mapping(source = "claimCount", target = "claimCount")
  PaymentReceipt toReceipt(Payment payment, long claimCount);

  // ── Model / DTO → DTO / model (controller layer) ────────────────────────────────────────────

  RecordPaymentRequest toModel(RecordPaymentRequestDto dto);

  ReceivedPaymentDto toDto(ReceivedPayment model);

  AwaitedPaymentDto toDto(AwaitedPayment model);

  ClaimAccountingSummaryDto toDto(ClaimAccountingSummary model);

  PendingPayoutDto toDto(PendingPayout model);

  PaymentReceiptDto toDto(PaymentReceipt model);
}
