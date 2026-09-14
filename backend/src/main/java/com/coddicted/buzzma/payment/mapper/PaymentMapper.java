package com.coddicted.buzzma.payment.mapper;

import com.coddicted.buzzma.claim.dto.ClaimAccountingSummaryDto;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimAccounting;
import com.coddicted.buzzma.claim.model.ClaimAccountingSummary;
import com.coddicted.buzzma.payment.dto.AwaitedPaymentDto;
import com.coddicted.buzzma.payment.dto.MadePaymentDto;
import com.coddicted.buzzma.payment.dto.PaidPayoutDto;
import com.coddicted.buzzma.payment.dto.PaymentReceiptDto;
import com.coddicted.buzzma.payment.dto.PendingPayoutDto;
import com.coddicted.buzzma.payment.dto.ReceivedPaymentDto;
import com.coddicted.buzzma.payment.dto.RecordPaymentRequestDto;
import com.coddicted.buzzma.payment.entity.Payment;
import com.coddicted.buzzma.payment.model.AwaitedPayment;
import com.coddicted.buzzma.payment.model.MadePayment;
import com.coddicted.buzzma.payment.model.PaidPayout;
import com.coddicted.buzzma.payment.model.PaymentReceipt;
import com.coddicted.buzzma.payment.model.PendingPayout;
import com.coddicted.buzzma.payment.model.ReceivedPayment;
import com.coddicted.buzzma.payment.model.RecordPaymentRequest;
import com.coddicted.buzzma.payment.persistence.projection.AwaitedPaymentProjection;
import com.coddicted.buzzma.payment.persistence.projection.MadePaymentProjection;
import com.coddicted.buzzma.payment.persistence.projection.PaidPayoutProjection;
import com.coddicted.buzzma.payment.persistence.projection.PendingPayoutProjection;
import com.coddicted.buzzma.payment.persistence.projection.ReceivedPaymentProjection;
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

  @Mapping(source = "ca.id", target = "id")
  @Mapping(source = "ca.campaignId", target = "campaignId")
  @Mapping(source = "ca.dealId", target = "dealId")
  @Mapping(source = "ca.createdAt", target = "createdAt")
  @Mapping(source = "ca.mediatorReceivablePaise", target = "amountPaise")
  @Mapping(source = "claim.code", target = "claimCode")
  @Mapping(source = "claim.ecommerceOrderId", target = "ecommerceOrderId")
  ClaimAccountingSummary toSummaryForAgency(ClaimAccounting ca, Claim claim);

  @Mapping(source = "ca.id", target = "id")
  @Mapping(source = "ca.campaignId", target = "campaignId")
  @Mapping(source = "ca.dealId", target = "dealId")
  @Mapping(source = "ca.createdAt", target = "createdAt")
  @Mapping(source = "ca.buyerReceivablePaise", target = "amountPaise")
  @Mapping(source = "claim.code", target = "claimCode")
  @Mapping(source = "claim.ecommerceOrderId", target = "ecommerceOrderId")
  ClaimAccountingSummary toSummaryForMediator(ClaimAccounting ca, Claim claim);

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

  PaidPayout toPaidPayout(PaidPayoutProjection projection);

  @Mapping(source = "projection.paymentId", target = "paymentId")
  @Mapping(source = "projection.claimCount", target = "claimCount")
  @Mapping(source = "projection.totalAmountPaise", target = "totalAmountPaise")
  @Mapping(source = "projection.paidAt", target = "paidAt")
  @Mapping(source = "payment.paymentMethod", target = "paymentMethod")
  @Mapping(source = "payment.screenshotStorageKey", target = "screenshotStorageKey")
  MadePayment toMadePayment(MadePaymentProjection projection, Payment payment);

  // ── Model / DTO → DTO / model (controller layer) ────────────────────────────────────────────

  RecordPaymentRequest toModel(RecordPaymentRequestDto dto);

  ReceivedPaymentDto toDto(ReceivedPayment model);

  AwaitedPaymentDto toDto(AwaitedPayment model);

  ClaimAccountingSummaryDto toDto(ClaimAccountingSummary model);

  PendingPayoutDto toDto(PendingPayout model);

  PaymentReceiptDto toDto(PaymentReceipt model);

  PaidPayoutDto toDto(PaidPayout model);

  MadePaymentDto toDto(MadePayment model);
}
