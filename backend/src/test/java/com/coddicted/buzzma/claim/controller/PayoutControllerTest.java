package com.coddicted.buzzma.claim.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.coddicted.buzzma.claim.dto.RecordPaymentRequestDto;
import com.coddicted.buzzma.claim.entity.PaymentMethod;
import com.coddicted.buzzma.claim.mapper.PaymentMapperImpl;
import com.coddicted.buzzma.claim.model.ClaimAccountingSummary;
import com.coddicted.buzzma.claim.model.PaymentReceipt;
import com.coddicted.buzzma.claim.model.PendingPayout;
import com.coddicted.buzzma.claim.model.RecordPaymentRequest;
import com.coddicted.buzzma.claim.service.PayoutService;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.persistence.UsersRepository;
import com.coddicted.buzzma.shared.security.JwtService;
import com.coddicted.buzzma.shared.security.TestSecurityConfig;
import com.coddicted.buzzma.shared.security.WithBuzzmaUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PayoutController.class)
@Import({TestSecurityConfig.class, PaymentMapperImpl.class})
class PayoutControllerTest {

  private static final String CALLER_ID_STR = "11111111-1111-1111-1111-111111111111";
  private static final UUID CALLER_ID = UUID.fromString(CALLER_ID_STR);
  private static final UUID PAYEE_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockBean private JwtService jwtService;
  @MockBean private UsersRepository usersRepository;
  @MockBean private PayoutService payoutService;

  // ── listPending ───────────────────────────────────────────────────────────────────────────────

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_AGENCY, id = CALLER_ID_STR)
  void listPending_agencyRole_returns200() throws Exception {
    final PendingPayout model =
        PendingPayout.builder()
            .payeeId(PAYEE_ID)
            .claimCount(4)
            .totalAmountPaise(BigInteger.valueOf(40000))
            .oldestClaimAt(Instant.parse("2025-04-01T00:00:00Z"))
            .build();
    when(payoutService.listPending(CALLER_ID, UserRole.ROLE_AGENCY)).thenReturn(List.of(model));

    mockMvc
        .perform(get("/api/v1/payouts/pending"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].claimCount").value(4))
        .andExpect(jsonPath("$[0].totalAmountPaise").value(40000));
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_MEDIATOR, id = CALLER_ID_STR)
  void listPending_mediatorRole_returns200() throws Exception {
    when(payoutService.listPending(CALLER_ID, UserRole.ROLE_MEDIATOR)).thenReturn(List.of());

    mockMvc.perform(get("/api/v1/payouts/pending")).andExpect(status().isOk());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_BUYER)
  void listPending_buyerRole_returns403() throws Exception {
    mockMvc.perform(get("/api/v1/payouts/pending")).andExpect(status().isForbidden());
  }

  @Test
  void listPending_unauthenticated_returns401() throws Exception {
    mockMvc.perform(get("/api/v1/payouts/pending")).andExpect(status().isUnauthorized());
  }

  // ── listClaims ────────────────────────────────────────────────────────────────────────────────

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_AGENCY, id = CALLER_ID_STR)
  void listClaims_agencyRole_returns200() throws Exception {
    final ClaimAccountingSummary model =
        ClaimAccountingSummary.builder()
            .id(UUID.randomUUID())
            .claimId(UUID.randomUUID())
            .amountPaise(BigInteger.valueOf(10000))
            .build();
    when(payoutService.listClaimsForPayee(CALLER_ID, PAYEE_ID, UserRole.ROLE_AGENCY))
        .thenReturn(List.of(model));

    mockMvc
        .perform(get("/api/v1/payouts/{payeeId}/claims", PAYEE_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].amountPaise").value(10000));
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_MEDIATOR, id = CALLER_ID_STR)
  void listClaims_mediatorRole_returns200() throws Exception {
    when(payoutService.listClaimsForPayee(CALLER_ID, PAYEE_ID, UserRole.ROLE_MEDIATOR))
        .thenReturn(List.of());

    mockMvc.perform(get("/api/v1/payouts/{payeeId}/claims", PAYEE_ID)).andExpect(status().isOk());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_BUYER)
  void listClaims_buyerRole_returns403() throws Exception {
    mockMvc
        .perform(get("/api/v1/payouts/{payeeId}/claims", PAYEE_ID))
        .andExpect(status().isForbidden());
  }

  // ── pay ───────────────────────────────────────────────────────────────────────────────────────

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_AGENCY, id = CALLER_ID_STR)
  void pay_agencyRoleWithValidBody_returns201() throws Exception {
    final RecordPaymentRequestDto request =
        new RecordPaymentRequestDto(
            PaymentMethod.UPI, Instant.parse("2025-06-01T00:00:00Z"), "UTR12345", null, null);
    final PaymentReceipt receipt =
        PaymentReceipt.builder()
            .id(UUID.randomUUID())
            .payerId(CALLER_ID)
            .payeeId(PAYEE_ID)
            .amountPaidPaise(BigInteger.valueOf(50000))
            .claimCount(5)
            .paymentMethod(PaymentMethod.UPI)
            .paidAt(Instant.parse("2025-06-01T00:00:00Z"))
            .build();
    when(payoutService.pay(
            eq(CALLER_ID),
            eq(PAYEE_ID),
            eq(UserRole.ROLE_AGENCY),
            any(RecordPaymentRequest.class),
            any(),
            any(),
            any()))
        .thenReturn(receipt);

    final MockMultipartFile screenshot =
        new MockMultipartFile("screenshot", "proof.png", "image/png", new byte[1]);
    final MockMultipartFile requestPart =
        new MockMultipartFile(
            "request", "", "application/json", objectMapper.writeValueAsBytes(request));

    mockMvc
        .perform(
            multipart("/api/v1/payouts/{payeeId}/pay", PAYEE_ID).file(screenshot).file(requestPart))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.claimCount").value(5))
        .andExpect(jsonPath("$.paymentMethod").value("UPI"));
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_BUYER)
  void pay_buyerRole_returns403() throws Exception {
    final MockMultipartFile screenshot =
        new MockMultipartFile("screenshot", "proof.png", "image/png", new byte[1]);
    final MockMultipartFile requestPart =
        new MockMultipartFile(
            "request",
            "",
            "application/json",
            objectMapper.writeValueAsBytes(
                new RecordPaymentRequestDto(
                    PaymentMethod.UPI, Instant.parse("2025-06-01T00:00:00Z"), null, null, null)));

    mockMvc
        .perform(
            multipart("/api/v1/payouts/{payeeId}/pay", PAYEE_ID).file(screenshot).file(requestPart))
        .andExpect(status().isForbidden());
  }

  @Test
  void pay_unauthenticated_returns401() throws Exception {
    final MockMultipartFile screenshot =
        new MockMultipartFile("screenshot", "proof.png", "image/png", new byte[1]);
    final MockMultipartFile requestPart =
        new MockMultipartFile(
            "request",
            "",
            "application/json",
            objectMapper.writeValueAsBytes(
                new RecordPaymentRequestDto(
                    PaymentMethod.UPI, Instant.parse("2025-06-01T00:00:00Z"), null, null, null)));

    mockMvc
        .perform(
            multipart("/api/v1/payouts/{payeeId}/pay", PAYEE_ID).file(screenshot).file(requestPart))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_AGENCY)
  void pay_missingRequiredFields_returns400() throws Exception {
    // Request part with empty JSON — fails @Valid because paymentMethod and paidAt are @NotNull
    final MockMultipartFile screenshot =
        new MockMultipartFile("screenshot", "proof.png", "image/png", new byte[1]);
    final MockMultipartFile requestPart =
        new MockMultipartFile("request", "", "application/json", "{}".getBytes());

    mockMvc
        .perform(
            multipart("/api/v1/payouts/{payeeId}/pay", PAYEE_ID).file(screenshot).file(requestPart))
        .andExpect(status().isBadRequest());
  }
}
