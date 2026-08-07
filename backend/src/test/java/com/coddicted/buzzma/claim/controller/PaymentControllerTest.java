package com.coddicted.buzzma.claim.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.coddicted.buzzma.claim.entity.PaymentMethod;
import com.coddicted.buzzma.claim.mapper.PaymentMapperImpl;
import com.coddicted.buzzma.claim.model.PaymentReceipt;
import com.coddicted.buzzma.claim.service.MyPaymentsService;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.persistence.UsersRepository;
import com.coddicted.buzzma.shared.security.JwtService;
import com.coddicted.buzzma.shared.security.TestSecurityConfig;
import com.coddicted.buzzma.shared.security.WithBuzzmaUser;
import java.math.BigInteger;
import java.time.Instant;
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

  @MockBean private JwtService jwtService;
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
}
