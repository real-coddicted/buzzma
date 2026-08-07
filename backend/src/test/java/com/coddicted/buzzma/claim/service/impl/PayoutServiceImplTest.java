package com.coddicted.buzzma.claim.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.claim.entity.AccountingPaymentStatus;
import com.coddicted.buzzma.claim.entity.ClaimAccounting;
import com.coddicted.buzzma.claim.entity.Payment;
import com.coddicted.buzzma.claim.entity.PaymentMethod;
import com.coddicted.buzzma.claim.mapper.PaymentMapper;
import com.coddicted.buzzma.claim.model.ClaimAccountingSummary;
import com.coddicted.buzzma.claim.model.PaymentReceipt;
import com.coddicted.buzzma.claim.model.PendingPayout;
import com.coddicted.buzzma.claim.model.RecordPaymentRequest;
import com.coddicted.buzzma.claim.persistence.ClaimAccountingRepository;
import com.coddicted.buzzma.claim.persistence.PaymentRepository;
import com.coddicted.buzzma.claim.persistence.projection.PendingPayoutProjection;
import com.coddicted.buzzma.identity.entity.UserRole;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class PayoutServiceImplTest {

  private static final UUID AGENCY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID MEDIATOR_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID BUYER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
  private static final UUID PAYMENT_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
  private static final UUID CLAIM_ACCOUNTING_ID =
      UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
  private static final UUID CLAIM_ID = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");
  private static final UUID CAMPAIGN_ID = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
  private static final Instant PAID_AT = Instant.parse("2025-06-01T00:00:00Z");

  @Mock private ClaimAccountingRepository claimAccountingRepository;
  @Mock private PaymentRepository paymentRepository;

  private PayoutServiceImpl service;

  @BeforeEach
  void setUp() {
    service =
        new PayoutServiceImpl(
            claimAccountingRepository, paymentRepository, Mappers.getMapper(PaymentMapper.class));
  }

  // ── listPending ───────────────────────────────────────────────────────────────────────────────

  @Test
  void listPending_agencyRole_delegatesToFindPendingByAgencyAndMapsModels() {
    final PendingPayoutProjection proj = mock(PendingPayoutProjection.class);
    when(proj.getPayeeId()).thenReturn(MEDIATOR_ID);
    when(proj.getClaimCount()).thenReturn(4L);
    when(proj.getTotalAmountPaise()).thenReturn(BigInteger.valueOf(40000));
    when(proj.getOldestClaimAt()).thenReturn(PAID_AT);
    when(claimAccountingRepository.findPendingByAgency(AGENCY_ID)).thenReturn(List.of(proj));

    final List<PendingPayout> result = service.listPending(AGENCY_ID, UserRole.ROLE_AGENCY);

    assertEquals(1, result.size());
    assertEquals(MEDIATOR_ID, result.get(0).getPayeeId());
    assertEquals(4, result.get(0).getClaimCount());
    assertEquals(BigInteger.valueOf(40000), result.get(0).getTotalAmountPaise());
    assertEquals(PAID_AT, result.get(0).getOldestClaimAt());
    verify(claimAccountingRepository).findPendingByAgency(AGENCY_ID);
  }

  @Test
  void listPending_mediatorRole_delegatesToFindPendingByMediatorAndMapsModels() {
    final PendingPayoutProjection proj = mock(PendingPayoutProjection.class);
    when(proj.getPayeeId()).thenReturn(BUYER_ID);
    when(proj.getClaimCount()).thenReturn(2L);
    when(proj.getTotalAmountPaise()).thenReturn(BigInteger.valueOf(20000));
    when(proj.getOldestClaimAt()).thenReturn(PAID_AT);
    when(claimAccountingRepository.findPendingByMediator(MEDIATOR_ID)).thenReturn(List.of(proj));

    final List<PendingPayout> result = service.listPending(MEDIATOR_ID, UserRole.ROLE_MEDIATOR);

    assertEquals(1, result.size());
    assertEquals(BUYER_ID, result.get(0).getPayeeId());
    verify(claimAccountingRepository).findPendingByMediator(MEDIATOR_ID);
  }

  @Test
  void listPending_forbiddenRole_throwsForbidden() {
    final ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () -> service.listPending(BUYER_ID, UserRole.ROLE_BUYER));

    assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
  }

  // ── listClaimsForPayee ────────────────────────────────────────────────────────────────────────

  @Test
  void listClaimsForPayee_agencyRole_returnsMediatorReceivableAmount() {
    final ClaimAccounting ca =
        ClaimAccounting.builder()
            .id(CLAIM_ACCOUNTING_ID)
            .claimId(CLAIM_ID)
            .campaignId(CAMPAIGN_ID)
            .agencyId(AGENCY_ID)
            .mediatorId(MEDIATOR_ID)
            .mediatorReceivablePaise(BigInteger.valueOf(10000))
            .buyerReceivablePaise(BigInteger.valueOf(8000))
            .build();
    when(claimAccountingRepository.findClaimsForMediatorPayout(AGENCY_ID, MEDIATOR_ID))
        .thenReturn(List.of(ca));

    final List<ClaimAccountingSummary> result =
        service.listClaimsForPayee(AGENCY_ID, MEDIATOR_ID, UserRole.ROLE_AGENCY);

    assertEquals(1, result.size());
    assertEquals(CLAIM_ACCOUNTING_ID, result.get(0).getId());
    assertEquals(CLAIM_ID, result.get(0).getClaimId());
    assertEquals(BigInteger.valueOf(10000), result.get(0).getAmountPaise());
  }

  @Test
  void listClaimsForPayee_mediatorRole_returnsBuyerReceivableAmount() {
    final ClaimAccounting ca =
        ClaimAccounting.builder()
            .id(CLAIM_ACCOUNTING_ID)
            .claimId(CLAIM_ID)
            .campaignId(CAMPAIGN_ID)
            .mediatorId(MEDIATOR_ID)
            .buyerId(BUYER_ID)
            .mediatorReceivablePaise(BigInteger.valueOf(10000))
            .buyerReceivablePaise(BigInteger.valueOf(8000))
            .build();
    when(claimAccountingRepository.findClaimsForBuyerPayout(MEDIATOR_ID, BUYER_ID))
        .thenReturn(List.of(ca));

    final List<ClaimAccountingSummary> result =
        service.listClaimsForPayee(MEDIATOR_ID, BUYER_ID, UserRole.ROLE_MEDIATOR);

    assertEquals(1, result.size());
    assertEquals(BigInteger.valueOf(8000), result.get(0).getAmountPaise());
    verify(claimAccountingRepository).findClaimsForBuyerPayout(MEDIATOR_ID, BUYER_ID);
  }

  @Test
  void listClaimsForPayee_forbiddenRole_throwsForbidden() {
    final ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () -> service.listClaimsForPayee(BUYER_ID, MEDIATOR_ID, UserRole.ROLE_BUYER));

    assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
  }

  // ── pay ───────────────────────────────────────────────────────────────────────────────────────

  @Test
  void pay_agencyFullBatch_savesPaymentAndMarksMediatorPaid() {
    final ClaimAccounting ca =
        ClaimAccounting.builder()
            .id(CLAIM_ACCOUNTING_ID)
            .agencyId(AGENCY_ID)
            .mediatorId(MEDIATOR_ID)
            .mediatorReceivablePaise(BigInteger.valueOf(10000))
            .build();
    when(claimAccountingRepository.findPendingByAgencyAndMediatorForUpdate(AGENCY_ID, MEDIATOR_ID))
        .thenReturn(List.of(ca));
    final Payment saved =
        Payment.builder()
            .id(PAYMENT_ID)
            .payerId(AGENCY_ID)
            .payeeId(MEDIATOR_ID)
            .amountPaidPaise(BigInteger.valueOf(10000))
            .paymentMethod(PaymentMethod.UPI)
            .paidAt(PAID_AT)
            .build();
    when(paymentRepository.saveAndFlush(any())).thenReturn(saved);

    final RecordPaymentRequest request =
        RecordPaymentRequest.builder()
            .paymentMethod(PaymentMethod.UPI)
            .paidAt(PAID_AT)
            .utrRef("UTR001")
            .build();
    final PaymentReceipt receipt =
        service.pay(AGENCY_ID, MEDIATOR_ID, UserRole.ROLE_AGENCY, request);

    assertEquals(PAYMENT_ID, receipt.getId());
    assertEquals(BigInteger.valueOf(10000), receipt.getAmountPaidPaise());
    assertEquals(1, receipt.getClaimCount());
    verify(claimAccountingRepository)
        .markMediatorPaid(
            eq(List.of(CLAIM_ACCOUNTING_ID)),
            eq(PAYMENT_ID),
            eq(PAID_AT),
            any(Instant.class),
            eq(AGENCY_ID));
  }

  @Test
  void pay_mediatorFullBatch_savesPaymentAndMarksBuyerPaid() {
    final ClaimAccounting ca =
        ClaimAccounting.builder()
            .id(CLAIM_ACCOUNTING_ID)
            .mediatorId(MEDIATOR_ID)
            .buyerId(BUYER_ID)
            .buyerReceivablePaise(BigInteger.valueOf(8000))
            .build();
    when(claimAccountingRepository.findPendingByMediatorAndBuyerForUpdate(MEDIATOR_ID, BUYER_ID))
        .thenReturn(List.of(ca));
    final Payment saved =
        Payment.builder()
            .id(PAYMENT_ID)
            .payerId(MEDIATOR_ID)
            .payeeId(BUYER_ID)
            .amountPaidPaise(BigInteger.valueOf(8000))
            .paymentMethod(PaymentMethod.BANK)
            .paidAt(PAID_AT)
            .build();
    when(paymentRepository.saveAndFlush(any())).thenReturn(saved);

    final RecordPaymentRequest request =
        RecordPaymentRequest.builder().paymentMethod(PaymentMethod.BANK).paidAt(PAID_AT).build();
    final PaymentReceipt receipt =
        service.pay(MEDIATOR_ID, BUYER_ID, UserRole.ROLE_MEDIATOR, request);

    assertEquals(PAYMENT_ID, receipt.getId());
    assertEquals(BigInteger.valueOf(8000), receipt.getAmountPaidPaise());
    verify(claimAccountingRepository)
        .markBuyerPaid(
            eq(List.of(CLAIM_ACCOUNTING_ID)),
            eq(PAYMENT_ID),
            eq(PAID_AT),
            any(Instant.class),
            eq(MEDIATOR_ID));
  }

  @Test
  void pay_partialBatch_usesLockedClaimsFromSuppliedIds() {
    final ClaimAccounting ca =
        ClaimAccounting.builder()
            .id(CLAIM_ACCOUNTING_ID)
            .agencyId(AGENCY_ID)
            .mediatorId(MEDIATOR_ID)
            .mediatorReceivablePaise(BigInteger.valueOf(10000))
            .build();
    when(claimAccountingRepository.findByIdInForUpdate(List.of(CLAIM_ACCOUNTING_ID)))
        .thenReturn(List.of(ca));
    when(paymentRepository.saveAndFlush(any()))
        .thenReturn(
            Payment.builder()
                .id(PAYMENT_ID)
                .payerId(AGENCY_ID)
                .payeeId(MEDIATOR_ID)
                .amountPaidPaise(BigInteger.valueOf(10000))
                .paymentMethod(PaymentMethod.UPI)
                .paidAt(PAID_AT)
                .build());

    final RecordPaymentRequest request =
        RecordPaymentRequest.builder()
            .paymentMethod(PaymentMethod.UPI)
            .paidAt(PAID_AT)
            .claimIds(List.of(CLAIM_ACCOUNTING_ID))
            .build();
    final PaymentReceipt receipt =
        service.pay(AGENCY_ID, MEDIATOR_ID, UserRole.ROLE_AGENCY, request);

    assertEquals(1, receipt.getClaimCount());
    verify(claimAccountingRepository).findByIdInForUpdate(List.of(CLAIM_ACCOUNTING_ID));
  }

  @Test
  void pay_noEligibleClaims_throwsBadRequest() {
    when(claimAccountingRepository.findPendingByAgencyAndMediatorForUpdate(AGENCY_ID, MEDIATOR_ID))
        .thenReturn(List.of());

    final RecordPaymentRequest request =
        RecordPaymentRequest.builder().paymentMethod(PaymentMethod.UPI).paidAt(PAID_AT).build();
    final ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () -> service.pay(AGENCY_ID, MEDIATOR_ID, UserRole.ROLE_AGENCY, request));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
  }

  @Test
  void pay_alreadyPaidClaim_throwsConflict() {
    final ClaimAccounting ca =
        ClaimAccounting.builder()
            .id(CLAIM_ACCOUNTING_ID)
            .agencyId(AGENCY_ID)
            .mediatorId(MEDIATOR_ID)
            .mediatorPaymentStatus(AccountingPaymentStatus.PAID)
            .mediatorReceivablePaise(BigInteger.valueOf(10000))
            .build();
    when(claimAccountingRepository.findPendingByAgencyAndMediatorForUpdate(AGENCY_ID, MEDIATOR_ID))
        .thenReturn(List.of(ca));

    final RecordPaymentRequest request =
        RecordPaymentRequest.builder().paymentMethod(PaymentMethod.UPI).paidAt(PAID_AT).build();
    final ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () -> service.pay(AGENCY_ID, MEDIATOR_ID, UserRole.ROLE_AGENCY, request));

    assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
  }

  @Test
  void pay_partialBatchCountMismatch_throwsBadRequest() {
    final UUID missingId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    // Only 1 of the 2 requested claimIds is found
    final ClaimAccounting ca = ClaimAccounting.builder().id(CLAIM_ACCOUNTING_ID).build();
    when(claimAccountingRepository.findByIdInForUpdate(List.of(CLAIM_ACCOUNTING_ID, missingId)))
        .thenReturn(List.of(ca));

    final RecordPaymentRequest request =
        RecordPaymentRequest.builder()
            .paymentMethod(PaymentMethod.UPI)
            .paidAt(PAID_AT)
            .claimIds(List.of(CLAIM_ACCOUNTING_ID, missingId))
            .build();
    final ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () -> service.pay(AGENCY_ID, MEDIATOR_ID, UserRole.ROLE_AGENCY, request));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
  }

  @Test
  void pay_partialBatchOwnershipMismatch_throwsBadRequest() {
    final UUID wrongAgencyId = UUID.fromString("99999999-9999-9999-9999-999999999999");
    final ClaimAccounting ca =
        ClaimAccounting.builder()
            .id(CLAIM_ACCOUNTING_ID)
            .agencyId(wrongAgencyId)
            .mediatorId(MEDIATOR_ID)
            .build();
    when(claimAccountingRepository.findByIdInForUpdate(List.of(CLAIM_ACCOUNTING_ID)))
        .thenReturn(List.of(ca));

    final RecordPaymentRequest request =
        RecordPaymentRequest.builder()
            .paymentMethod(PaymentMethod.UPI)
            .paidAt(PAID_AT)
            .claimIds(List.of(CLAIM_ACCOUNTING_ID))
            .build();
    final ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () -> service.pay(AGENCY_ID, MEDIATOR_ID, UserRole.ROLE_AGENCY, request));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
  }
}
