package com.coddicted.buzzma.identity.service.impl;

import static com.coddicted.buzzma.identity.service.impl.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.EmailOtp;
import com.coddicted.buzzma.identity.entity.VerifiedEmail;
import com.coddicted.buzzma.identity.persistence.EmailOtpRepository;
import com.coddicted.buzzma.identity.persistence.UsersRepository;
import com.coddicted.buzzma.identity.persistence.VerifiedEmailRepository;
import com.coddicted.buzzma.identity.service.OtpGenerator;
import com.coddicted.buzzma.identity.template.OtpEmailTemplate;
import com.coddicted.buzzma.shared.communications.CommunicationsClient;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceImplTest {

  @Mock private UsersRepository mockUsersRepository;
  @Mock private EmailOtpRepository mockEmailOtpRepository;
  @Mock private VerifiedEmailRepository mockVerifiedEmailRepository;
  @Mock private CommunicationsClient mockCommunicationsClient;
  @Mock private OtpGenerator mockOtpGenerator;
  @Mock private OtpEmailTemplate mockOtpEmailTemplate;

  private EmailVerificationServiceImpl service;

  @BeforeEach
  void setUp() {
    this.service =
        new EmailVerificationServiceImpl(
            this.mockUsersRepository,
            this.mockEmailOtpRepository,
            this.mockVerifiedEmailRepository,
            this.mockCommunicationsClient,
            this.mockOtpGenerator,
            this.mockOtpEmailTemplate);
  }

  @Test
  void sendOtpThrowsWhenUserNotFound() {
    when(this.mockUsersRepository.findById(USER_ID)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> this.service.sendOtp(USER_ID));
  }

  @Test
  void sendOtpThrowsWhenUserHasNoEmail() {
    when(this.mockUsersRepository.findById(USER_ID)).thenReturn(Optional.of(USER_2));

    final BusinessRuleViolationException ex =
        assertThrows(BusinessRuleViolationException.class, () -> this.service.sendOtp(USER_ID));
    assertEquals("User has no email on file", ex.getMessage());
  }

  @Test
  void sendOtpThrowsWhenResendRequestedWithinCooldown() {
    when(this.mockUsersRepository.findById(USER_ID)).thenReturn(Optional.of(USER_WITH_EMAIL));
    final EmailOtp recentOtp =
        EmailOtp.builder()
            .userId(USER_ID)
            .otpHash("irrelevant")
            .expiresAt(Instant.now().plusSeconds(900))
            .createdAt(Instant.now().minusSeconds(10))
            .build();
    when(this.mockEmailOtpRepository.findTopByUserIdOrderByCreatedAtDesc(USER_ID))
        .thenReturn(Optional.of(recentOtp));

    final BusinessRuleViolationException ex =
        assertThrows(BusinessRuleViolationException.class, () -> this.service.sendOtp(USER_ID));
    assertEquals("Please wait before requesting another OTP", ex.getMessage());
    verify(this.mockEmailOtpRepository, never()).deleteByUserId(USER_ID);
    verify(this.mockCommunicationsClient, never()).sendEmail(any(), any(), any(), any());
  }

  @Test
  void sendOtpGeneratesStoresAndSendsEmailWhenNoCooldownActive() {
    when(this.mockUsersRepository.findById(USER_ID)).thenReturn(Optional.of(USER_WITH_EMAIL));
    final EmailOtp oldOtp =
        EmailOtp.builder()
            .userId(USER_ID)
            .otpHash("irrelevant")
            .expiresAt(Instant.now().minusSeconds(1))
            .createdAt(Instant.now().minusSeconds(120))
            .build();
    when(this.mockEmailOtpRepository.findTopByUserIdOrderByCreatedAtDesc(USER_ID))
        .thenReturn(Optional.of(oldOtp));
    when(this.mockOtpGenerator.generate()).thenReturn(OTP_CODE);
    when(this.mockOtpEmailTemplate.subject()).thenReturn("Your Buzzma verification code");
    when(this.mockOtpEmailTemplate.render(OTP_CODE)).thenReturn("body with " + OTP_CODE);

    this.service.sendOtp(USER_ID);

    verify(this.mockEmailOtpRepository).deleteByUserId(USER_ID);

    final ArgumentCaptor<EmailOtp> captor = ArgumentCaptor.forClass(EmailOtp.class);
    verify(this.mockEmailOtpRepository).save(captor.capture());
    final EmailOtp saved = captor.getValue();
    assertEquals(USER_ID, saved.getUserId());
    assertEquals(sha256Hex(OTP_CODE), saved.getOtpHash());
    assertTrue(saved.getExpiresAt().isAfter(Instant.now()));
    assertEquals(USER_ID, saved.getCreatedBy());
    assertEquals(USER_ID, saved.getUpdatedBy());

    verify(this.mockCommunicationsClient)
        .sendEmail(USER_EMAIL, "Your Buzzma verification code", "body with " + OTP_CODE, USER_ID);
  }

  @Test
  void verifyOtpThrowsWhenNoOtpRequested() {
    when(this.mockEmailOtpRepository.findTopByUserIdOrderByCreatedAtDesc(USER_ID))
        .thenReturn(Optional.empty());

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class, () -> this.service.verifyOtp(USER_ID, OTP_CODE));
    assertEquals("No OTP requested for this user", ex.getMessage());
  }

  @Test
  void verifyOtpThrowsWhenAlreadyConsumed() {
    final EmailOtp consumed =
        EmailOtp.builder()
            .userId(USER_ID)
            .otpHash(sha256Hex(OTP_CODE))
            .expiresAt(Instant.now().plusSeconds(900))
            .createdAt(Instant.now().minusSeconds(10))
            .isUsed(true)
            .build();
    when(this.mockEmailOtpRepository.findTopByUserIdOrderByCreatedAtDesc(USER_ID))
        .thenReturn(Optional.of(consumed));

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class, () -> this.service.verifyOtp(USER_ID, OTP_CODE));
    assertEquals("OTP already used", ex.getMessage());
  }

  @Test
  void verifyOtpThrowsWhenExpired() {
    final EmailOtp expired =
        EmailOtp.builder()
            .userId(USER_ID)
            .otpHash(sha256Hex(OTP_CODE))
            .expiresAt(Instant.now().minusSeconds(1))
            .createdAt(Instant.now().minusSeconds(1000))
            .build();
    when(this.mockEmailOtpRepository.findTopByUserIdOrderByCreatedAtDesc(USER_ID))
        .thenReturn(Optional.of(expired));

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class, () -> this.service.verifyOtp(USER_ID, OTP_CODE));
    assertEquals("OTP expired", ex.getMessage());
  }

  @Test
  void verifyOtpThrowsWhenCodeDoesNotMatch() {
    final EmailOtp valid =
        EmailOtp.builder()
            .userId(USER_ID)
            .otpHash(sha256Hex(OTP_CODE))
            .expiresAt(Instant.now().plusSeconds(900))
            .createdAt(Instant.now().minusSeconds(10))
            .build();
    when(this.mockEmailOtpRepository.findTopByUserIdOrderByCreatedAtDesc(USER_ID))
        .thenReturn(Optional.of(valid));

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class, () -> this.service.verifyOtp(USER_ID, "999999"));
    assertEquals("Invalid OTP", ex.getMessage());
  }

  @Test
  void verifyOtpMarksConsumedAndVerifiesUserWhenCodeMatches() {
    final EmailOtp valid =
        EmailOtp.builder()
            .userId(USER_ID)
            .otpHash(sha256Hex(OTP_CODE))
            .expiresAt(Instant.now().plusSeconds(900))
            .createdAt(Instant.now().minusSeconds(10))
            .build();
    when(this.mockEmailOtpRepository.findTopByUserIdOrderByCreatedAtDesc(USER_ID))
        .thenReturn(Optional.of(valid));
    when(this.mockUsersRepository.findById(USER_ID)).thenReturn(Optional.of(USER_WITH_EMAIL));

    this.service.verifyOtp(USER_ID, OTP_CODE);

    final ArgumentCaptor<EmailOtp> otpCaptor = ArgumentCaptor.forClass(EmailOtp.class);
    verify(this.mockEmailOtpRepository).save(otpCaptor.capture());
    assertTrue(otpCaptor.getValue().getIsUsed());
    assertEquals(USER_ID, otpCaptor.getValue().getUpdatedBy());

    final ArgumentCaptor<BuzzmaUser> userCaptor = ArgumentCaptor.forClass(BuzzmaUser.class);
    verify(this.mockUsersRepository).save(userCaptor.capture());
    assertTrue(userCaptor.getValue().getEmailVerified());
    assertEquals(USER_ID, userCaptor.getValue().getUpdatedBy());

    final ArgumentCaptor<VerifiedEmail> verifiedEmailCaptor =
        ArgumentCaptor.forClass(VerifiedEmail.class);
    verify(this.mockVerifiedEmailRepository).save(verifiedEmailCaptor.capture());
    final VerifiedEmail verifiedEmail = verifiedEmailCaptor.getValue();
    assertEquals(USER_ID, verifiedEmail.getUserId());
    assertEquals(USER_EMAIL, verifiedEmail.getEmail());
    assertEquals(USER_WITH_EMAIL.getName(), verifiedEmail.getFullName());
    assertEquals(USER_ID, verifiedEmail.getCreatedBy());
    assertEquals(USER_ID, verifiedEmail.getUpdatedBy());
  }

  private static String sha256Hex(final String value) {
    try {
      final MessageDigest digest = MessageDigest.getInstance("SHA-256");
      final byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
      final StringBuilder hex = new StringBuilder(64);
      for (final byte b : bytes) {
        hex.append(String.format("%02x", b));
      }
      return hex.toString();
    } catch (final Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
