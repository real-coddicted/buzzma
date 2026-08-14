package com.coddicted.buzzma.claim.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.coddicted.buzzma.claim.entity.PaymentMethod;
import com.coddicted.buzzma.claim.mapper.PaymentMapperImpl;
import com.coddicted.buzzma.claim.model.AwaitedPayment;
import com.coddicted.buzzma.claim.model.ClaimAccountingSummary;
import com.coddicted.buzzma.claim.model.PaymentReceipt;
import com.coddicted.buzzma.claim.model.ReceivedPayment;
import com.coddicted.buzzma.claim.service.MyPaymentsService;
import com.coddicted.buzzma.config.ConfigProvider;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.persistence.UsersRepository;
import com.coddicted.buzzma.shared.security.JwtService;
import com.coddicted.buzzma.shared.security.TestSecurityConfig;
import com.coddicted.buzzma.shared.security.WithBuzzmaUser;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(PaymentController.class)
@Import({TestSecurityConfig.class, PaymentMapperImpl.class})
class PaymentControllerTest {

  private static final String CALLER_ID_STR = "11111111-1111-1111-1111-111111111111";
  private static final UUID CALLER_ID = UUID.fromString(CALLER_ID_STR);
  private static final UUID PAYMENT_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
  private static final UUID PAYER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID PAYEE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

  @Autowired private MockMvc mockMvc;

  // JwtAuthenticationFilter and RequestLoggingFilter are @Component Filters scanned by
  // @WebMvcTest — mock their deps
  @MockBean private JwtService jwtService;
  @MockBean private ConfigProvider configProvider;
  @MockBean private UsersRepository usersRepository;
  @MockBean private MyPaymentsService myPaymentsService;

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_MEDIATOR, id = CALLER_ID_STR)
  void getReceipt_authenticated_returns200WithReceiptData() throws Exception {
    final PaymentReceipt receipt =
        PaymentReceipt.builder()
            .id(PAYMENT_ID)
            .payerId(PAYER_ID)
            .payeeId(PAYEE_ID)
            .amountPaidPaise(BigInteger.valueOf(50000))
            .claimCount(5)
            .paymentMethod(PaymentMethod.UPI)
            .utrRef("UTR12345")
            .paidAt(Instant.parse("2025-06-01T00:00:00Z"))
            .build();
    when(myPaymentsService.getReceipt(PAYMENT_ID, CALLER_ID)).thenReturn(receipt);

    mockMvc
        .perform(get("/api/v1/payments/{id}", PAYMENT_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.claimCount").value(5))
        .andExpect(jsonPath("$.paymentMethod").value("UPI"))
        .andExpect(jsonPath("$.utrRef").value("UTR12345"));
  }

  @Test
  void getReceipt_unauthenticated_returns401() throws Exception {
    mockMvc.perform(get("/api/v1/payments/{id}", PAYMENT_ID)).andExpect(status().isUnauthorized());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_BUYER, id = CALLER_ID_STR)
  void getReceipt_paymentNotFound_returns404() throws Exception {
    when(myPaymentsService.getReceipt(PAYMENT_ID, CALLER_ID))
        .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));

    mockMvc.perform(get("/api/v1/payments/{id}", PAYMENT_ID)).andExpect(status().isNotFound());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_BUYER, id = CALLER_ID_STR)
  void getReceipt_callerNotAParty_returns403() throws Exception {
    when(myPaymentsService.getReceipt(PAYMENT_ID, CALLER_ID))
        .thenThrow(
            new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a party to this payment"));

    mockMvc.perform(get("/api/v1/payments/{id}", PAYMENT_ID)).andExpect(status().isForbidden());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_MEDIATOR, id = CALLER_ID_STR)
  void listReceived_mediatorRole_returns200WithMappedDtos() throws Exception {
    final ReceivedPayment model =
        ReceivedPayment.builder()
            .paymentId(UUID.randomUUID())
            .payerId(PAYER_ID)
            .claimCount(3)
            .totalAmountPaise(BigInteger.valueOf(30000))
            .paidAt(Instant.parse("2025-06-01T00:00:00Z"))
            .build();
    when(myPaymentsService.listReceived(CALLER_ID, UserRole.ROLE_MEDIATOR))
        .thenReturn(List.of(model));

    mockMvc
        .perform(get("/api/v1/my-payments/received"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].claimCount").value(3))
        .andExpect(jsonPath("$[0].totalAmountPaise").value(30000));
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_BUYER, id = CALLER_ID_STR)
  void listReceived_buyerRole_returns200() throws Exception {
    when(myPaymentsService.listReceived(CALLER_ID, UserRole.ROLE_BUYER)).thenReturn(List.of());

    mockMvc.perform(get("/api/v1/my-payments/received")).andExpect(status().isOk());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_AGENCY)
  void listReceived_agencyRole_returns403() throws Exception {
    mockMvc.perform(get("/api/v1/my-payments/received")).andExpect(status().isForbidden());
  }

  @Test
  void listReceived_unauthenticated_returns401() throws Exception {
    mockMvc.perform(get("/api/v1/my-payments/received")).andExpect(status().isUnauthorized());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_MEDIATOR, id = CALLER_ID_STR)
  void listAwaited_mediatorRole_returns200WithMappedDtos() throws Exception {
    final AwaitedPayment model =
        AwaitedPayment.builder()
            .counterpartyId(PAYER_ID)
            .claimCount(2)
            .totalAmountPaise(BigInteger.valueOf(20000))
            .oldestClaimAt(Instant.parse("2025-05-01T00:00:00Z"))
            .build();
    when(myPaymentsService.listAwaited(CALLER_ID, UserRole.ROLE_MEDIATOR))
        .thenReturn(List.of(model));

    mockMvc
        .perform(get("/api/v1/my-payments/awaited"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].claimCount").value(2))
        .andExpect(jsonPath("$[0].totalAmountPaise").value(20000));
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_BUYER, id = CALLER_ID_STR)
  void listAwaited_buyerRole_returns200() throws Exception {
    when(myPaymentsService.listAwaited(CALLER_ID, UserRole.ROLE_BUYER)).thenReturn(List.of());

    mockMvc.perform(get("/api/v1/my-payments/awaited")).andExpect(status().isOk());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_AGENCY)
  void listAwaited_agencyRole_returns403() throws Exception {
    mockMvc.perform(get("/api/v1/my-payments/awaited")).andExpect(status().isForbidden());
  }

  @Test
  void listAwaited_unauthenticated_returns401() throws Exception {
    mockMvc.perform(get("/api/v1/my-payments/awaited")).andExpect(status().isUnauthorized());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_MEDIATOR, id = CALLER_ID_STR)
  void listAwaitedClaims_mediatorRole_returns200WithMappedDtos() throws Exception {
    final ClaimAccountingSummary model =
        ClaimAccountingSummary.builder()
            .id(UUID.randomUUID())
            .claimId(UUID.randomUUID())
            .campaignId(UUID.randomUUID())
            .dealId(UUID.randomUUID())
            .amountPaise(BigInteger.valueOf(10000))
            .createdAt(Instant.parse("2025-05-01T00:00:00Z"))
            .build();
    when(myPaymentsService.listAwaitedClaims(PAYER_ID, CALLER_ID, UserRole.ROLE_MEDIATOR))
        .thenReturn(List.of(model));

    mockMvc
        .perform(get("/api/v1/my-payments/awaited/{counterpartyId}/claims", PAYER_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].amountPaise").value(10000));
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_BUYER, id = CALLER_ID_STR)
  void listAwaitedClaims_buyerRole_returns200WithMappedDtos() throws Exception {
    final ClaimAccountingSummary model =
        ClaimAccountingSummary.builder()
            .id(UUID.randomUUID())
            .claimId(UUID.randomUUID())
            .campaignId(UUID.randomUUID())
            .dealId(UUID.randomUUID())
            .amountPaise(BigInteger.valueOf(5000))
            .createdAt(Instant.parse("2025-05-01T00:00:00Z"))
            .build();
    when(myPaymentsService.listAwaitedClaims(PAYER_ID, CALLER_ID, UserRole.ROLE_BUYER))
        .thenReturn(List.of(model));

    mockMvc
        .perform(get("/api/v1/my-payments/awaited/{counterpartyId}/claims", PAYER_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].amountPaise").value(5000));
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_AGENCY)
  void listAwaitedClaims_agencyRole_returns403() throws Exception {
    mockMvc
        .perform(get("/api/v1/my-payments/awaited/{counterpartyId}/claims", PAYER_ID))
        .andExpect(status().isForbidden());
  }

  @Test
  void listAwaitedClaims_unauthenticated_returns401() throws Exception {
    mockMvc
        .perform(get("/api/v1/my-payments/awaited/{counterpartyId}/claims", PAYER_ID))
        .andExpect(status().isUnauthorized());
  }
}
