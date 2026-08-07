package com.coddicted.buzzma.claim.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.claim.entity.Payment;
import com.coddicted.buzzma.claim.entity.PaymentMethod;
import com.coddicted.buzzma.claim.mapper.PaymentMapper;
import com.coddicted.buzzma.claim.model.AwaitedPayment;
import com.coddicted.buzzma.claim.model.PaymentReceipt;
import com.coddicted.buzzma.claim.model.ReceivedPayment;
import com.coddicted.buzzma.claim.persistence.ClaimAccountingRepository;
import com.coddicted.buzzma.claim.persistence.PaymentRepository;
import com.coddicted.buzzma.claim.persistence.projection.AwaitedPaymentProjection;
import com.coddicted.buzzma.claim.persistence.projection.ReceivedPaymentProjection;
import com.coddicted.buzzma.identity.entity.UserRole;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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

  @Mock private ClaimAccountingRepository claimAccountingRepository;
  @Mock private PaymentRepository paymentRepository;

  private MyPaymentsServiceImpl service;

  @BeforeEach
  void setUp() {
    service =
        new MyPaymentsServiceImpl(
            claimAccountingRepository, paymentRepository, Mappers.getMapper(PaymentMapper.class));
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
    when(claimAccountingRepository.findReceivedByMediator(CALLER_ID)).thenReturn(List.of(proj));

    final List<ReceivedPayment> result = service.listReceived(CALLER_ID, UserRole.ROLE_MEDIATOR);

    assertEquals(1, result.size());
    assertEquals(PAYMENT_ID, result.get(0).getPaymentId());
    assertEquals(PAYER_ID, result.get(0).getPayerId());
    assertEquals(3, result.get(0).getClaimCount());
    assertEquals(BigInteger.valueOf(30000), result.get(0).getTotalAmountPaise());
    assertEquals(PAID_AT, result.get(0).getPaidAt());
    verify(claimAccountingRepository).findReceivedByMediator(CALLER_ID);
  }

  @Test
  void listReceived_buyerRole_callsFindReceivedByBuyerAndMapsModels() {
    final ReceivedPaymentProjection proj = mock(ReceivedPaymentProjection.class);
    when(proj.getPaymentId()).thenReturn(PAYMENT_ID);
    when(proj.getPayerId()).thenReturn(PAYER_ID);
    when(proj.getClaimCount()).thenReturn(1L);
    when(proj.getTotalAmountPaise()).thenReturn(BigInteger.valueOf(10000));
    when(proj.getPaidAt()).thenReturn(PAID_AT);
    when(claimAccountingRepository.findReceivedByBuyer(CALLER_ID)).thenReturn(List.of(proj));

    final List<ReceivedPayment> result = service.listReceived(CALLER_ID, UserRole.ROLE_BUYER);

    assertEquals(1, result.size());
    assertEquals(PAYMENT_ID, result.get(0).getPaymentId());
    verify(claimAccountingRepository).findReceivedByBuyer(CALLER_ID);
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
    when(claimAccountingRepository.findAwaitedByMediator(CALLER_ID)).thenReturn(List.of(proj));

    final List<AwaitedPayment> result = service.listAwaited(CALLER_ID, UserRole.ROLE_MEDIATOR);

    assertEquals(1, result.size());
    assertEquals(PAYER_ID, result.get(0).getCounterpartyId());
    assertEquals(2, result.get(0).getClaimCount());
    assertEquals(BigInteger.valueOf(20000), result.get(0).getTotalAmountPaise());
    assertEquals(PAID_AT, result.get(0).getOldestClaimAt());
    verify(claimAccountingRepository).findAwaitedByMediator(CALLER_ID);
  }

  @Test
  void listAwaited_buyerRole_callsFindAwaitedByBuyerAndMapsModels() {
    final AwaitedPaymentProjection proj = mock(AwaitedPaymentProjection.class);
    when(proj.getCounterpartyId()).thenReturn(PAYER_ID);
    when(proj.getClaimCount()).thenReturn(1L);
    when(proj.getTotalAmountPaise()).thenReturn(BigInteger.valueOf(5000));
    when(proj.getOldestClaimAt()).thenReturn(PAID_AT);
    when(claimAccountingRepository.findAwaitedByBuyer(CALLER_ID)).thenReturn(List.of(proj));

    final List<AwaitedPayment> result = service.listAwaited(CALLER_ID, UserRole.ROLE_BUYER);

    assertEquals(1, result.size());
    assertEquals(PAYER_ID, result.get(0).getCounterpartyId());
    verify(claimAccountingRepository).findAwaitedByBuyer(CALLER_ID);
  }

  @Test
  void listAwaited_forbiddenRole_throwsForbidden() {
    final ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () -> service.listAwaited(CALLER_ID, UserRole.ROLE_AGENCY));

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
    when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));
    when(claimAccountingRepository.countByMediatorPaymentId(PAYMENT_ID)).thenReturn(3L);
    when(claimAccountingRepository.countByBuyerPaymentId(PAYMENT_ID)).thenReturn(2L);

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
    when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));
    when(claimAccountingRepository.countByMediatorPaymentId(PAYMENT_ID)).thenReturn(1L);
    when(claimAccountingRepository.countByBuyerPaymentId(PAYMENT_ID)).thenReturn(0L);

    final PaymentReceipt receipt = service.getReceipt(PAYMENT_ID, CALLER_ID);

    assertEquals(PAYER_ID, receipt.getPayerId());
    assertEquals(CALLER_ID, receipt.getPayeeId());
    assertEquals(1, receipt.getClaimCount());
  }

  @Test
  void getReceipt_paymentNotFound_throwsNotFound() {
    when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.empty());

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
    when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));

    final ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class, () -> service.getReceipt(PAYMENT_ID, unrelatedCallerId));

    assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
  }
}
