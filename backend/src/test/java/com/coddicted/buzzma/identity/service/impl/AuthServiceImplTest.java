package com.coddicted.buzzma.identity.service.impl;

import static com.coddicted.buzzma.identity.service.impl.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.connection.service.ConnectionService;
import com.coddicted.buzzma.identity.entity.UserCredential;
import com.coddicted.buzzma.identity.persistence.SecurityAnswerRepository;
import com.coddicted.buzzma.identity.persistence.SecurityQuestionRepository;
import com.coddicted.buzzma.identity.persistence.UserCredentialRepository;
import com.coddicted.buzzma.identity.service.RefreshTokenService;
import com.coddicted.buzzma.identity.service.UserBankingDetailService;
import com.coddicted.buzzma.identity.service.UserService;
import com.coddicted.buzzma.invite.service.InviteService;
import com.coddicted.buzzma.settings.service.UserSettingsService;
import com.coddicted.buzzma.shared.common.PasswordService;
import com.coddicted.buzzma.shared.exception.UnauthorizedException;
import com.coddicted.buzzma.shared.security.JwtProperties;
import com.coddicted.buzzma.shared.security.JwtService;
import com.coddicted.buzzma.terms.service.TermsService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

  @Mock private JwtService mockJwtService;
  @Mock private JwtProperties mockJwtProperties;
  @Mock private RefreshTokenService mockRefreshTokenService;
  @Mock private UserService mockUserService;
  @Mock private UserBankingDetailService mockUserBankingDetailService;
  @Mock private InviteService mockInviteService;
  @Mock private ConnectionService mockConnectionService;
  @Mock private UserSettingsService mockUserSettingsService;
  @Mock private TermsService mockTermsService;
  @Mock private UserCredentialRepository mockCredentialRepository;
  @Mock private SecurityQuestionRepository mockQuestionRepository;
  @Mock private SecurityAnswerRepository mockAnswerRepository;
  @Mock private PasswordService mockPasswordService;

  private AuthServiceImpl authService;

  @BeforeEach
  void setUp() {
    this.authService =
        new AuthServiceImpl(
            this.mockJwtService,
            this.mockJwtProperties,
            this.mockRefreshTokenService,
            this.mockUserService,
            new UserCredentialServiceImpl(this.mockCredentialRepository, this.mockPasswordService),
            this.mockUserBankingDetailService,
            this.mockInviteService,
            new SecurityQuestionAnswerServiceImpl(
                this.mockQuestionRepository, this.mockAnswerRepository, this.mockPasswordService),
            this.mockConnectionService,
            this.mockUserSettingsService,
            this.mockTermsService);
  }

  @Test
  void testResetPasswordWithCorrectAnswerUpdatesPasswordAndRevokesSessions() {
    when(this.mockUserService.getByMobile(MOBILE)).thenReturn(USER_2);
    when(this.mockAnswerRepository.findByUserIdAndQuestionId(USER_ID, QUESTION_ID))
        .thenReturn(Optional.of(EXPECTED_SECURITY_ANSWER_1));
    when(this.mockPasswordService.verifyPassword(PLAIN_ANSWER, STORED_ANSWER_HASH))
        .thenReturn(true);
    when(this.mockCredentialRepository.findByUserIdAndIsDeletedFalse(USER_ID))
        .thenReturn(Optional.of(USER_CREDENTIAL_2));
    when(this.mockPasswordService.verifyPassword(PLAIN_PASSWORD, STORED_HASH)).thenReturn(false);
    when(this.mockPasswordService.hashPassword(PLAIN_PASSWORD)).thenReturn(NEW_HASH);

    final boolean result =
        this.authService.resetPassword(MOBILE, QUESTION_ID, PLAIN_ANSWER, PLAIN_PASSWORD);

    assertTrue(result);
    final ArgumentCaptor<UserCredential> captor = ArgumentCaptor.forClass(UserCredential.class);
    verify(this.mockCredentialRepository).save(captor.capture());
    assertEquals(NEW_HASH, captor.getValue().getPasswordHash());
    assertEquals(USER_ID, captor.getValue().getUpdatedBy());
    verify(this.mockRefreshTokenService).revokeAll(USER_ID);
  }

  @Test
  void testResetPasswordWithWrongAnswerIsRejected() {
    when(this.mockUserService.getByMobile(MOBILE)).thenReturn(USER_2);
    when(this.mockAnswerRepository.findByUserIdAndQuestionId(USER_ID, QUESTION_ID))
        .thenReturn(Optional.of(EXPECTED_SECURITY_ANSWER_1));
    when(this.mockPasswordService.verifyPassword(PLAIN_ANSWER, STORED_ANSWER_HASH))
        .thenReturn(false);

    final UnauthorizedException ex =
        assertThrows(
            UnauthorizedException.class,
            () ->
                this.authService.resetPassword(MOBILE, QUESTION_ID, PLAIN_ANSWER, PLAIN_PASSWORD));
    assertEquals("Incorrect security answer", ex.getMessage());
    verifyNoInteractions(this.mockCredentialRepository, this.mockRefreshTokenService);
  }

  @Test
  void testResetPasswordWithUnansweredQuestionIsRejected() {
    when(this.mockUserService.getByMobile(MOBILE)).thenReturn(USER_2);
    when(this.mockAnswerRepository.findByUserIdAndQuestionId(USER_ID, QUESTION_ID))
        .thenReturn(Optional.empty());

    final UnauthorizedException ex =
        assertThrows(
            UnauthorizedException.class,
            () ->
                this.authService.resetPassword(MOBILE, QUESTION_ID, PLAIN_ANSWER, PLAIN_PASSWORD));
    assertEquals("Incorrect security answer", ex.getMessage());
    verifyNoInteractions(this.mockCredentialRepository, this.mockRefreshTokenService);
  }
}
