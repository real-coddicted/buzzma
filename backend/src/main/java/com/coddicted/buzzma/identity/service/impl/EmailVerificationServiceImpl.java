package com.coddicted.buzzma.identity.service.impl;

import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.EmailOtp;
import com.coddicted.buzzma.identity.entity.VerifiedEmail;
import com.coddicted.buzzma.identity.persistence.EmailOtpRepository;
import com.coddicted.buzzma.identity.persistence.UsersRepository;
import com.coddicted.buzzma.identity.persistence.VerifiedEmailRepository;
import com.coddicted.buzzma.identity.service.EmailVerificationService;
import com.coddicted.buzzma.identity.service.OtpGenerator;
import com.coddicted.buzzma.identity.template.OtpEmailTemplate;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.communications.CommunicationsClient;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailVerificationServiceImpl extends BaseCrudService
    implements EmailVerificationService {

  private static final Duration OTP_TTL = Duration.ofMinutes(15);
  private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);

  private final UsersRepository usersRepository;
  private final EmailOtpRepository emailOtpRepository;
  private final VerifiedEmailRepository verifiedEmailRepository;
  private final CommunicationsClient communicationsClient;
  private final OtpGenerator otpGenerator;
  private final OtpEmailTemplate otpEmailTemplate;

  public EmailVerificationServiceImpl(
      final UsersRepository usersRepository,
      final EmailOtpRepository emailOtpRepository,
      final VerifiedEmailRepository verifiedEmailRepository,
      final CommunicationsClient communicationsClient,
      final OtpGenerator otpGenerator,
      final OtpEmailTemplate otpEmailTemplate) {
    this.usersRepository = usersRepository;
    this.emailOtpRepository = emailOtpRepository;
    this.verifiedEmailRepository = verifiedEmailRepository;
    this.communicationsClient = communicationsClient;
    this.otpGenerator = otpGenerator;
    this.otpEmailTemplate = otpEmailTemplate;
  }

  @Override
  @Transactional
  public void sendOtp(final UUID userId) {
    final BuzzmaUser user = mustFind(this.usersRepository, userId, "Users");
    if (user.getEmail() == null || user.getEmail().isBlank()) {
      throw new BusinessRuleViolationException("User has no email on file");
    }

    this.emailOtpRepository
        .findTopByUserIdOrderByCreatedAtDesc(userId)
        .ifPresent(
            latest -> {
              final Instant cooldownEnd = latest.getCreatedAt().plus(RESEND_COOLDOWN);
              if (Instant.now().isBefore(cooldownEnd)) {
                throw new BusinessRuleViolationException(
                    "Please wait before requesting another OTP");
              }
            });

    final String otp = this.otpGenerator.generate();
    this.emailOtpRepository.deleteByUserId(userId);
    this.emailOtpRepository.save(
        EmailOtp.builder()
            .userId(userId)
            .otpHash(hash(otp))
            .expiresAt(Instant.now().plus(OTP_TTL))
            .createdBy(userId)
            .updatedBy(userId)
            .build());

    this.communicationsClient.sendEmail(
        user.getEmail(),
        this.otpEmailTemplate.subject(),
        this.otpEmailTemplate.render(otp),
        userId);
  }

  @Override
  @Transactional
  public void verifyOtp(final UUID userId, final String code) {
    final EmailOtp latest =
        this.emailOtpRepository
            .findTopByUserIdOrderByCreatedAtDesc(userId)
            .orElseThrow(
                () -> new BusinessRuleViolationException("No OTP requested for this user"));

    if (latest.getIsUsed()) {
      throw new BusinessRuleViolationException("OTP already used");
    }
    if (Instant.now().isAfter(latest.getExpiresAt())) {
      throw new BusinessRuleViolationException("OTP expired");
    }
    if (!latest.getOtpHash().equals(hash(code))) {
      throw new BusinessRuleViolationException("Invalid OTP");
    }

    latest.setIsUsed(true);
    latest.setUpdatedBy(userId);
    this.emailOtpRepository.save(latest);

    final BuzzmaUser user = mustFind(this.usersRepository, userId, "Users");
    this.usersRepository.save(user.toBuilder().emailVerified(true).updatedBy(userId).build());

    this.verifiedEmailRepository.save(
        VerifiedEmail.builder()
            .userId(userId)
            .email(user.getEmail())
            .fullName(user.getName())
            .createdBy(userId)
            .updatedBy(userId)
            .build());
  }

  private String hash(final String value) {
    try {
      final MessageDigest digest = MessageDigest.getInstance("SHA-256");
      final byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
      final StringBuilder hex = new StringBuilder(64);
      for (final byte b : bytes) {
        hex.append(String.format("%02x", b));
      }
      return hex.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }
}
