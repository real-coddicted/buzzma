package com.coddicted.buzzma.identity.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.coddicted.buzzma.identity.mapper.AuthMapper;
import com.coddicted.buzzma.identity.mapper.SecurityQuestionMapper;
import com.coddicted.buzzma.identity.persistence.UsersRepository;
import com.coddicted.buzzma.identity.service.AuthService;
import com.coddicted.buzzma.shared.security.CookieProperties;
import com.coddicted.buzzma.shared.security.JwtProperties;
import com.coddicted.buzzma.shared.security.JwtService;
import com.coddicted.buzzma.shared.turnstile.TurnstileClient;
import com.coddicted.buzzma.shared.util.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // /api/v1/auth/** is public; no auth to simulate here
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  // JwtAuthenticationFilter is a @Component Filter scanned by @WebMvcTest — mock its deps
  @MockBean private JwtService jwtService;
  @MockBean private UsersRepository usersRepository;

  @MockBean private AuthService authService;
  @MockBean private AuthMapper authMapper;
  @MockBean private SecurityQuestionMapper securityQuestionMapper;
  @MockBean private JwtProperties jwtProperties;
  @MockBean private CookieProperties cookieProperties;
  @MockBean private TurnstileClient turnstileClient;

  private static final String VALID_BODY =
      FileUtils.loadResourceAsString("/fixtures/input/identity/user-registration-request-1.json");

  // --- POST /api/v1/auth/register ---

  @Test
  void testRegisterWithValidEmailReturns201() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
        .andExpect(status().isCreated());

    verify(authService).register(any(), any(), any(), any(), any(), any());
  }

  @Test
  void testRegisterWithMissingEmailReturnsBadRequest() throws Exception {
    final String body = VALID_BODY.replace("\"email\": \"test@example.com\",\n  ", "");

    mockMvc
        .perform(
            post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testRegisterWithBlankEmailReturnsBadRequest() throws Exception {
    final String body = VALID_BODY.replace("test@example.com", "");

    mockMvc
        .perform(
            post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testRegisterWithMalformedEmailReturnsBadRequest() throws Exception {
    final String body = VALID_BODY.replace("test@example.com", "not-an-email");

    mockMvc
        .perform(
            post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());
  }
}
