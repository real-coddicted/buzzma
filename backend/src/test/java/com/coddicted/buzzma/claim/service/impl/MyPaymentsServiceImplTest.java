package com.coddicted.buzzma.claim.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.claim.entity.ClaimAccounting;
import com.coddicted.buzzma.claim.entity.Payment;
import com.coddicted.buzzma.claim.entity.PaymentMethod;
import com.coddicted.buzzma.claim.mapper.PaymentMapper;
import com.coddicted.buzzma.claim.model.AwaitedPayment;
import com.coddicted.buzzma.claim.model.ClaimAccountingSummary;
import com.coddicted.buzzma.claim.model.PaymentReceipt;
import com.coddicted.buzzma.claim.model.ReceivedPayment;
import com.coddicted.buzzma.claim.persistence.projection.AwaitedPaymentProjection;
import com.coddicted.buzzma.claim.persistence.projection.ReceivedPaymentProjection;
import com.coddicted.buzzma.claim.service.ClaimAccountingService;
import com.coddicted.buzzma.claim.service.PaymentService;
import com.coddicted.buzzma.identity.entity.UserRole;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.Map;
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
class MyPaymentsServiceImplTest {

  private static final UUID CALLER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID PAYER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID PAYEE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
  private static final UUID PAYMENT_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
  private static final Instant PAID_AT = Instant.parse("2025-06-01T00:00:00Z");

  @Mock private ClaimAccountingService claimAccountingService;
  @Mock private PaymentService paymentService;

  private MyPaymentsServiceImpl service;

  @BeforeEach
  void setUp() {
    service =
        new MyPaymentsServiceImpl(
            claimAccountingService, paymentService, Mappers.getMapper(PaymentMapper.class));
  }

  // ── listReceived ──────────────────────────────────────────────────────────────────────────────

  @Test
  void listReceived_mediatorRole_callsFindReceivedByMediatorAndMapsModels() {
    final ReceivedPaymentProjection proj = mock(ReceivedPaymentProjection.class);
    when(proj.getPaymentId()).thenReturn(PAYMENT_ID);
    when(proj.getPayerId()).thenReturn(PAYER_ID);
    when(proj.getClaimCount()).thenReturn(3L);
    when(proj.getTotalAmountPaise()).thenReturn(BigInteger.valueOf(30000));
    when(proj.getPaidAt()).thenReturn(PAID_AT);
    when(claimAccountingService.findReceivedByMediator(CALLER_ID)).thenReturn(List.of(proj));
    when(paymentService.findAllByIdAsMap(List.of(PAYMENT_ID))).thenReturn(Map.of());

    final List<ReceivedPayment> result = service.listReceived(CALLER_ID, UserRole.ROLE_MEDIATOR);

    assertEquals(1, result.size());
    assertEquals(PAYMENT_ID, result.get(0).getPaymentId());
    assertEquals(PAYER_ID, result.get(0).getPayerId());
    assertEquals(3, result.get(0).getClaimCount());
    assertEquals(BigInteger.valueOf(30000), result.get(0).getTotalAmountPaise());
    assertEquals(PAID_AT, result.get(0).getPaidAt());
    verify(claimAccountingService).findReceivedByMediator(CALLER_ID);
  }

  @Test
  void listReceived_buyerRole_callsFindReceivedByBuyerAndMapsModels() {
    final ReceivedPaymentProjection proj = mock(ReceivedPaymentProjection.class);
    when(proj.getPaymentId()).thenReturn(PAYMENT_ID);
    when(proj.getPayerId()).thenReturn(PAYER_ID);
    when(proj.getClaimCount()).thenReturn(1L);
    when(proj.getTotalAmountPaise()).thenReturn(BigInteger.valueOf(10000));
    when(proj.getPaidAt()).thenReturn(PAID_AT);
    when(claimAccountingService.findReceivedByBuyer(CALLER_ID)).thenReturn(List.of(proj));
    when(paymentService.findAllByIdAsMap(List.of(PAYMENT_ID))).thenReturn(Map.of());

    final List<ReceivedPayment> result = service.listReceived(CALLER_ID, UserRole.ROLE_BUYER);

    assertEquals(1, result.size());
    assertEquals(PAYMENT_ID, result.get(0).getPaymentId());
    verify(claimAccountingService).findReceivedByBuyer(CALLER_ID);
  }

  @Test
  void listReceived_forbiddenRole_throwsForbidden() {
    final ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () -> service.listReceived(CALLER_ID, UserRole.ROLE_AGENCY));

    assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
  }

  // ── listAwaited ──────────────────────────────────────────────────────────────────────────────

  @Test
  void listAwaited_mediatorRole_callsFindAwaitedByMediatorAndMapsModels() {
    final AwaitedPaymentProjection proj = mock(AwaitedPaymentProjection.class);
    when(proj.getCounterpartyId()).thenReturn(PAYER_ID);
    when(proj.getClaimCount()).thenReturn(2L);
    when(proj.getTotalAmountPaise()).thenReturn(BigInteger.valueOf(20000));
    when(proj.getOldestClaimAt()).thenReturn(PAID_AT);
    when(claimAccountingService.findAwaitedByMediator(CALLER_ID)).thenReturn(List.of(proj));

    final List<AwaitedPayment> result = service.listAwaited(CALLER_ID, UserRole.ROLE_MEDIATOR);

    assertEquals(1, result.size());
    assertEquals(PAYER_ID, result.get(0).getCounterpartyId());
    assertEquals(2, result.get(0).getClaimCount());
    assertEquals(BigInteger.valueOf(20000), result.get(0).getTotalAmountPaise());
    assertEquals(PAID_AT, result.get(0).getOldestClaimAt());
    verify(claimAccountingService).findAwaitedByMediator(CALLER_ID);
  }

  @Test
  void listAwaited_buyerRole_callsFindAwaitedByBuyerAndMapsModels() {
    final AwaitedPaymentProjection proj = mock(AwaitedPaymentProjection.class);
    when(proj.getCounterpartyId()).thenReturn(PAYER_ID);
    when(proj.getClaimCount()).thenReturn(1L);
    when(proj.getTotalAmountPaise()).thenReturn(BigInteger.valueOf(5000));
    when(proj.getOldestClaimAt()).thenReturn(PAID_AT);
    when(claimAccountingService.findAwaitedByBuyer(CALLER_ID)).thenReturn(List.of(proj));

    final List<AwaitedPayment> result = service.listAwaited(CALLER_ID, UserRole.ROLE_BUYER);

    assertEquals(1, result.size());
    assertEquals(PAYER_ID, result.get(0).getCounterpartyId());
    verify(claimAccountingService).findAwaitedByBuyer(CALLER_ID);
  }

  @Test
  void listAwaited_forbiddenRole_throwsForbidden() {
    final ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () -> service.listAwaited(CALLER_ID, UserRole.ROLE_AGENCY));

    assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
  }

  // ── listAwaitedClaims ─────────────────────────────────────────────────────────────────────────

  @Test
  void listAwaitedClaims_mediatorRole_callsFindClaimsForMediatorPayoutAndMapsWithAgencyAmount() {
    final ClaimAccounting ca =
        ClaimAccounting.builder()
            .id(UUID.randomUUID())
            .claimId(UUID.randomUUID())
            .campaignId(UUID.randomUUID())
            .dealId(UUID.randomUUID())
            .mediatorReceivablePaise(BigInteger.valueOf(10000))
            .createdAt(Instant.parse("2025-05-01T00:00:00Z"))
            .build();
    when(claimAccountingService.findClaimsPendingForMediatorPayout(PAYER_ID, CALLER_ID))
        .thenReturn(List.of(ca));

    final List<ClaimAccountingSummary> result =
        service.listAwaitedClaims(PAYER_ID, CALLER_ID, UserRole.ROLE_MEDIATOR);

    assertEquals(1, result.size());
    assertEquals(BigInteger.valueOf(10000), result.get(0).getAmountPaise());
    verify(claimAccountingService).findClaimsPendingForMediatorPayout(PAYER_ID, CALLER_ID);
  }

  @Test
  void listAwaitedClaims_buyerRole_callsFindClaimsForBuyerPayoutAndMapsWithMediatorAmount() {
    final ClaimAccounting ca =
        ClaimAccounting.builder()
            .id(UUID.randomUUID())
            .claimId(UUID.randomUUID())
            .campaignId(UUID.randomUUID())
            .dealId(UUID.randomUUID())
            .buyerReceivablePaise(BigInteger.valueOf(5000))
            .createdAt(Instant.parse("2025-05-01T00:00:00Z"))
            .build();
    when(claimAccountingService.findClaimsPendingForBuyerPayout(PAYER_ID, CALLER_ID))
        .thenReturn(List.of(ca));

    final List<ClaimAccountingSummary> result =
        service.listAwaitedClaims(PAYER_ID, CALLER_ID, UserRole.ROLE_BUYER);

    assertEquals(1, result.size());
    assertEquals(BigInteger.valueOf(5000), result.get(0).getAmountPaise());
    verify(claimAccountingService).findClaimsPendingForBuyerPayout(PAYER_ID, CALLER_ID);
  }

  @Test
  void listAwaitedClaims_forbiddenRole_throwsForbidden() {
    final ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () -> service.listAwaitedClaims(PAYER_ID, CALLER_ID, UserRole.ROLE_AGENCY));

    assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
  }

  // ── listReceivedClaims ────────────────────────────────────────────────────────────────────────

  @Test
  void listReceivedClaims_mediatorRole_callsFindClaimsByMediatorPaymentIdAndMapsWithAgencyAmount() {
    final ClaimAccounting ca =
        ClaimAccounting.builder()
            .id(UUID.randomUUID())
            .claimId(UUID.randomUUID())
            .campaignId(UUID.randomUUID())
            .dealId(UUID.randomUUID())
            .mediatorReceivablePaise(BigInteger.valueOf(10000))
            .createdAt(Instant.parse("2025-05-01T00:00:00Z"))
            .build();
    when(claimAccountingService.findClaimsByMediatorPaymentId(CALLER_ID, PAYMENT_ID))
        .thenReturn(List.of(ca));

    final List<ClaimAccountingSummary> result =
        service.listReceivedClaims(PAYMENT_ID, CALLER_ID, UserRole.ROLE_MEDIATOR);

    assertEquals(1, result.size());
    assertEquals(BigInteger.valueOf(10000), result.get(0).getAmountPaise());
    verify(claimAccountingService).findClaimsByMediatorPaymentId(CALLER_ID, PAYMENT_ID);
  }

  @Test
  void listReceivedClaims_buyerRole_callsFindClaimsByBuyerPaymentIdAndMapsWithMediatorAmount() {
    final ClaimAccounting ca =
        ClaimAccounting.builder()
            .id(UUID.randomUUID())
            .claimId(UUID.randomUUID())
            .campaignId(UUID.randomUUID())
            .dealId(UUID.randomUUID())
            .buyerReceivablePaise(BigInteger.valueOf(5000))
            .createdAt(Instant.parse("2025-05-01T00:00:00Z"))
            .build();
    when(claimAccountingService.findClaimsByBuyerPaymentId(CALLER_ID, PAYMENT_ID))
        .thenReturn(List.of(ca));

    final List<ClaimAccountingSummary> result =
        service.listReceivedClaims(PAYMENT_ID, CALLER_ID, UserRole.ROLE_BUYER);

    assertEquals(1, result.size());
    assertEquals(BigInteger.valueOf(5000), result.get(0).getAmountPaise());
    verify(claimAccountingService).findClaimsByBuyerPaymentId(CALLER_ID, PAYMENT_ID);
  }

  @Test
  void listReceivedClaims_forbiddenRole_throwsForbidden() {
    final ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () -> service.listReceivedClaims(PAYMENT_ID, CALLER_ID, UserRole.ROLE_AGENCY));

    assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
  }

  // ── getReceipt ────────────────────────────────────────────────────────────────────────────────

  @Test
  void getReceipt_callerIsPayer_returnsReceiptWithClaimCount() {
    final Payment payment =
        Payment.builder()
            .id(PAYMENT_ID)
            .payerId(CALLER_ID)
            .payeeId(PAYEE_ID)
            .amountPaidPaise(BigInteger.valueOf(50000))
            .paymentMethod(PaymentMethod.UPI)
            .utrRef("UTR12345")
            .paidAt(PAID_AT)
            .build();
    when(paymentService.getById(PAYMENT_ID)).thenReturn(payment);
    when(claimAccountingService.countByMediatorPaymentId(PAYMENT_ID)).thenReturn(3L);
    when(claimAccountingService.countByBuyerPaymentId(PAYMENT_ID)).thenReturn(2L);

    final PaymentReceipt receipt = service.getReceipt(PAYMENT_ID, CALLER_ID);

    assertEquals(PAYMENT_ID, receipt.getId());
    assertEquals(CALLER_ID, receipt.getPayerId());
    assertEquals(PAYEE_ID, receipt.getPayeeId());
    assertEquals(5, receipt.getClaimCount());
    assertEquals(PaymentMethod.UPI, receipt.getPaymentMethod());
    assertEquals("UTR12345", receipt.getUtrRef());
  }

  @Test
  void getReceipt_callerIsPayee_returnsReceipt() {
    final Payment payment =
        Payment.builder()
            .id(PAYMENT_ID)
            .payerId(PAYER_ID)
            .payeeId(CALLER_ID)
            .amountPaidPaise(BigInteger.valueOf(10000))
            .paymentMethod(PaymentMethod.BANK)
            .paidAt(PAID_AT)
            .build();
    when(paymentService.getById(PAYMENT_ID)).thenReturn(payment);
    when(claimAccountingService.countByMediatorPaymentId(PAYMENT_ID)).thenReturn(1L);
    when(claimAccountingService.countByBuyerPaymentId(PAYMENT_ID)).thenReturn(0L);

    final PaymentReceipt receipt = service.getReceipt(PAYMENT_ID, CALLER_ID);

    assertEquals(PAYER_ID, receipt.getPayerId());
    assertEquals(CALLER_ID, receipt.getPayeeId());
    assertEquals(1, receipt.getClaimCount());
  }

  @Test
  void getReceipt_paymentNotFound_throwsNotFound() {
    when(paymentService.getById(PAYMENT_ID))
        .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));

    final ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class, () -> service.getReceipt(PAYMENT_ID, CALLER_ID));

    assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
  }

  @Test
  void getReceipt_callerNotAParty_throwsForbidden() {
    final UUID unrelatedCallerId = UUID.fromString("99999999-9999-9999-9999-999999999999");
    final Payment payment =
        Payment.builder()
            .id(PAYMENT_ID)
            .payerId(PAYER_ID)
            .payeeId(PAYEE_ID)
            .amountPaidPaise(BigInteger.valueOf(10000))
            .paymentMethod(PaymentMethod.UPI)
            .paidAt(PAID_AT)
            .build();
    when(paymentService.getById(PAYMENT_ID)).thenReturn(payment);

    final ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class, () -> service.getReceipt(PAYMENT_ID, unrelatedCallerId));

    assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
  }
}
