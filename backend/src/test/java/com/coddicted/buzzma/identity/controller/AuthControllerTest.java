package com.coddicted.buzzma.identity.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.coddicted.buzzma.config.ConfigProvider;
import com.coddicted.buzzma.identity.mapper.AuthMapper;
import com.coddicted.buzzma.identity.mapper.SecurityQuestionMapper;
import com.coddicted.buzzma.identity.persistence.UsersRepository;
import com.coddicted.buzzma.identity.service.AuthService;
import com.coddicted.buzzma.shared.security.CookieProperties;
import com.coddicted.buzzma.shared.security.JwtProperties;
import com.coddicted.buzzma.shared.security.JwtService;
import com.coddicted.buzzma.shared.turnstile.TurnstileClient;
import com.coddicted.buzzma.shared.util.FileUtils;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // /api/v1/auth/** is public; no auth to simulate here
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  // JwtAuthenticationFilter and RequestLoggingFilter are @Component Filters scanned by
  // @WebMvcTest — mock their deps
  @MockBean private JwtService jwtService;
  @MockBean private ConfigProvider configProvider;
  @MockBean private UsersRepository usersRepository;

  @MockBean private AuthService authService;
  @MockBean private AuthMapper authMapper;
  @MockBean private SecurityQuestionMapper securityQuestionMapper;
  @MockBean private JwtProperties jwtProperties;
  @MockBean private CookieProperties cookieProperties;
  @MockBean private TurnstileClient turnstileClient;

  private static final String VALID_BODY =
      FileUtils.loadResourceAsString("/fixtures/input/identity/user-registration-request-1.json");

  private static final String VALID_RESET_BODY =
      FileUtils.loadResourceAsString("/fixtures/input/identity/password-reset-request-1.json");

  private static final String VALID_LOOKUP_BODY =
      FileUtils.loadResourceAsString(
          "/fixtures/input/identity/forgot-password-lookup-request-1.json");

  private static final String VALID_UPDATE_BODY =
      FileUtils.loadResourceAsString("/fixtures/input/identity/password-update-request-1.json");

  private static final String MOBILE = "9876543210";
  private static final UUID QUESTION_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
  private static final String ANSWER = "plain-answer";
  private static final String CURRENT_PASSWORD = "oldpass12";
  private static final String NEW_PASSWORD = "NewPass@123";

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  // Filters are off here, so set the principal an unauthenticated request really carries:
  // Spring's anonymous token, whose principal is the String "anonymousUser".
  private static void actAsAnonymousUser() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
  }

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

  @Test
  void testRegisterWithTermsNotAcceptedReturnsBadRequest() throws Exception {
    final String body = VALID_BODY.replace("\"termsAccepted\": true", "\"termsAccepted\": false");

    mockMvc
        .perform(
            post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testRegisterWithPasswordMissingSpecialCharacterReturnsBadRequest() throws Exception {
    final String body = VALID_BODY.replace("Password@123", "Password123");

    mockMvc
        .perform(
            post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.fields.password")
                .value("Password must contain at least one special character"));
  }

  @Test
  void testRegisterWithTooShortPasswordReturnsBadRequest() throws Exception {
    final String body = VALID_BODY.replace("Password@123", "Pa@1");

    mockMvc
        .perform(
            post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fields.password").value("Password must be at least 8 characters"));
  }

  // --- POST /api/v1/auth/forgot-password/lookup ---

  @Test
  void testForgotPasswordLookupAsAnonymousUserReturnsOk() throws Exception {
    actAsAnonymousUser();
    mockMvc
        .perform(
            post("/api/v1/auth/forgot-password/lookup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_LOOKUP_BODY))
        .andExpect(status().isOk());

    verify(authService).getSecurityQuestionsByMobile(MOBILE);
  }

  // --- POST /api/v1/auth/password-reset ---

  @Test
  void testPasswordResetAsAnonymousUserReturnsOk() throws Exception {
    actAsAnonymousUser();
    mockMvc
        .perform(
            post("/api/v1/auth/password-reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_RESET_BODY))
        .andExpect(status().isOk());

    verify(authService).resetPassword(MOBILE, QUESTION_ID, ANSWER, NEW_PASSWORD);
  }

  @Test
  void testPasswordResetWithValidPasswordReturnsOk() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/password-reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_RESET_BODY))
        .andExpect(status().isOk());

    verify(authService).resetPassword(MOBILE, QUESTION_ID, ANSWER, NEW_PASSWORD);
  }

  @Test
  void testPasswordResetWithWeakPasswordReturnsBadRequest() throws Exception {
    final String body = VALID_RESET_BODY.replace(NEW_PASSWORD, "newpass@123");

    mockMvc
        .perform(
            post("/api/v1/auth/password-reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.fields.newPassword")
                .value("Password must contain at least one uppercase letter"));
  }

  @Test
  void testPasswordResetWithoutSecurityAnswerReturnsBadRequest() throws Exception {
    final String body = VALID_RESET_BODY.replace("\"answer\": \"plain-answer\",\n  ", "");

    mockMvc
        .perform(
            post("/api/v1/auth/password-reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testPasswordResetWithoutQuestionIdReturnsBadRequest() throws Exception {
    final String body =
        VALID_RESET_BODY.replace(
            "\"questionId\": \"55555555-5555-5555-5555-555555555555\",\n  ", "");

    mockMvc
        .perform(
            post("/api/v1/auth/password-reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest());
  }

  // --- POST /api/v1/auth/password-update ---

  @Test
  void testPasswordUpdateWithValidPasswordReturnsOk() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/password-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_UPDATE_BODY))
        .andExpect(status().isOk());

    verify(authService).updatePassword(CURRENT_PASSWORD, NEW_PASSWORD, null);
  }

  @Test
  void testPasswordUpdateWithWeakPasswordReturnsBadRequest() throws Exception {
    final String body = VALID_UPDATE_BODY.replace(NEW_PASSWORD, "New Pass@123");

    mockMvc
        .perform(
            post("/api/v1/auth/password-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fields.newPassword").value("Password must not contain spaces"));
  }
}
