package com.coddicted.buzzma.identity.service.impl;

import com.coddicted.buzzma.connection.service.ConnectionService;
import com.coddicted.buzzma.identity.dto.auth.SignInResult;
import com.coddicted.buzzma.identity.dto.auth.TokensDto;
import com.coddicted.buzzma.identity.entity.BankDetails;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.SecurityAnswer;
import com.coddicted.buzzma.identity.entity.SecurityQuestionWrapper;
import com.coddicted.buzzma.identity.entity.UserBankingDetail;
import com.coddicted.buzzma.identity.entity.UserCredential;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.entity.UserStatus;
import com.coddicted.buzzma.identity.service.AuthService;
import com.coddicted.buzzma.identity.service.RefreshTokenService;
import com.coddicted.buzzma.identity.service.SecurityQuestionAnswerService;
import com.coddicted.buzzma.identity.service.UserBankingDetailService;
import com.coddicted.buzzma.identity.service.UserCredentialService;
import com.coddicted.buzzma.identity.service.UserService;
import com.coddicted.buzzma.invite.service.InviteService;
import com.coddicted.buzzma.settings.entity.UserSettings;
import com.coddicted.buzzma.settings.service.UserSettingsService;
import com.coddicted.buzzma.settings.util.SettingsUtils;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.exception.PasswordMatchException;
import com.coddicted.buzzma.shared.exception.UnauthorizedException;
import com.coddicted.buzzma.shared.security.JwtProperties;
import com.coddicted.buzzma.shared.security.JwtService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthServiceImpl implements AuthService {

  private static final Logger LOG = LoggerFactory.getLogger(AuthServiceImpl.class);

  private final JwtService jwtService;
  private final JwtProperties jwtProperties;
  private final RefreshTokenService refreshTokenService;
  private final UserService userService;
  private final UserCredentialService userCredentialService;
  private final UserBankingDetailService userBankingDetailService;
  private final SecurityQuestionAnswerService securityQuestionAnswerService;
  private final ConnectionService connectionService;
  private final UserSettingsService userSettingsService;

  public AuthServiceImpl(
      final JwtService jwtService,
      final JwtProperties jwtProperties,
      final RefreshTokenService refreshTokenService,
      final UserService userService,
      final UserCredentialService userCredentialService,
      final UserBankingDetailService userBankingDetailService,
      final InviteService inviteService,
      final SecurityQuestionAnswerService securityQuestionAnswerService,
      final ConnectionService connectionService,
      final UserSettingsService userSettingsService) {
    this.jwtService = jwtService;
    this.jwtProperties = jwtProperties;
    this.refreshTokenService = refreshTokenService;
    this.userService = userService;
    this.userCredentialService = userCredentialService;
    this.userBankingDetailService = userBankingDetailService;
    this.securityQuestionAnswerService = securityQuestionAnswerService;
    this.connectionService = connectionService;
    this.userSettingsService = userSettingsService;
  }

  @Override
  public List<SecurityQuestionWrapper> getSecurityQuestionsByMobile(
      final String mobile, final UUID requesterId) {
    final BuzzmaUser existingUser = this.userService.getByMobile(mobile);
    return this.securityQuestionAnswerService.getSecurityQuestionsByUserId(existingUser.getId());
  }

  @Override
  public boolean resetPassword(
      final String mobile, final String newPassword, final UUID requesterId) {
    final BuzzmaUser existingUser = this.userService.getByMobile(mobile);
    final UserCredential existingUserCredential =
        this.userCredentialService.getByUserId(existingUser.getId(), requesterId);
    final UserCredential updatedUserCredential =
        existingUserCredential.toBuilder().passwordHash(newPassword).build();
    return this.userCredentialService.update(updatedUserCredential, requesterId);
  }

  @Override
  @Transactional
  public BuzzmaUser register(
      final BuzzmaUser user,
      final UserCredential userCredential,
      final UserBankingDetail userBankingDetail,
      final List<SecurityAnswer> securityAnswerList,
      final String inviteCode,
      final UUID requesterId) {
    if (canRegister(user, userCredential, userBankingDetail, securityAnswerList)) {
      user.setStatus(UserStatus.USER_STATUS_ACTIVE);
      // save user
      final BuzzmaUser savedUser = this.userService.create(user);

      // save user credentials
      this.userCredentialService.create(
          userCredential.toBuilder().userId(savedUser.getId()).build(), requesterId);
      // save user security question's answers
      securityAnswerList.forEach(
          securityAnswer -> {
            securityAnswer.setUserId(savedUser.getId());
            this.securityQuestionAnswerService.createSecurityAnswer(securityAnswer);
          });
      // create connection between inviter and new user, then consume invite
      this.connectionService.createConnection(inviteCode, savedUser);

      // build and save userSettings with minimal pending-connection settings
      final UserSettings userSettings =
          UserSettings.builder()
              .settings(SettingsUtils.getPendingConnectionSettings())
              .userId(savedUser.getId())
              .build();

      this.userSettingsService.create(userSettings, requesterId);

      return savedUser;
    } else {
      LOG.warn("User {} not eligible to register", user.getUsername());
    }

    return null;
  }

  @Override
  public SignInResult signIn(final BuzzmaUser user, final UserCredential userCredential) {
    final BuzzmaUser existingUser = this.userService.getByMobile(user.getMobile());
    if (!this.userCredentialService.verify(
        existingUser.getId(), userCredential.getPasswordHash())) {
      throw new UnauthorizedException("Invalid credentials");
    }
    if (existingUser.getStatus() != UserStatus.USER_STATUS_ACTIVE) {
      throw new UnauthorizedException("Account is not active");
    }
    final TokensDto tokens = buildTokens(existingUser.getId());
    final Instant expiresAt = Instant.now().plusMillis(this.jwtProperties.getRefreshExpiryMs());
    this.refreshTokenService.issue(existingUser.getId(), tokens.getRefreshToken(), expiresAt);
    return new SignInResult(existingUser, tokens);
  }

  @Override
  public TokensDto refresh(final String refreshToken) {
    final UUID userId = this.jwtService.validateRefreshToken(refreshToken);
    final TokensDto newTokens = buildTokens(userId);
    final Instant expiresAt = Instant.now().plusMillis(this.jwtProperties.getRefreshExpiryMs());
    this.refreshTokenService.rotateToken(refreshToken, newTokens.getRefreshToken(), expiresAt);
    return newTokens;
  }

  private TokensDto buildTokens(final UUID userId) {
    return TokensDto.builder()
        .accessToken(this.jwtService.generateAccessToken(userId))
        .refreshToken(this.jwtService.generateRefreshToken(userId))
        .build();
  }

  @Override
  public void signOut(final String refreshToken) {
    this.refreshTokenService.revoke(refreshToken);
  }

  @Override
  public boolean updatePassword(
      final String currentPassword, final String newPassword, final UUID requesterId) {
    if (this.userCredentialService.verify(requesterId, currentPassword)) {
      final UserCredential existingUserCredential =
          this.userCredentialService.getByUserId(requesterId, requesterId);
      final UserCredential updatedUserCredential =
          existingUserCredential.toBuilder().passwordHash(newPassword).build();
      return this.userCredentialService.update(updatedUserCredential, requesterId);
    }
    throw new PasswordMatchException("Current password is incorrect");
  }

  private boolean canRegister(
      final BuzzmaUser user,
      final UserCredential userCredential,
      final UserBankingDetail userBankingDetail,
      final List<SecurityAnswer> securityAnswerList) {
    final boolean validUser = validateUser(user);
    //    final boolean validBankingDetails = validateUserBankingDetails(user, userBankingDetail);
    final boolean validSecurityAnswerList = validateSecurityAnswer(securityAnswerList);
    final boolean validPassword = validateUserCredential(userCredential);
    return validUser
        //        && validBankingDetails
        && validSecurityAnswerList
        && validPassword;
  }

  private boolean validateUser(final BuzzmaUser user) {
    if (this.userService.existsByMobile(user.getMobile())) {
      throw new BusinessRuleViolationException(
          "Registration failed. User with mobile number already exists");
    }
    return true;
  }

  private boolean validateUserBankingDetails(
      final BuzzmaUser user, final UserBankingDetail userBankingDetail) {
    if (user.getRole() == UserRole.ROLE_BUYER) {
      final BankDetails bankDetails = userBankingDetail.getBankDetails();
      return bankDetails != null
          && StringUtils.hasText(bankDetails.getBankName())
          && StringUtils.hasText(bankDetails.getAccountNumber())
          && StringUtils.hasText(bankDetails.getBankIfscCode())
          && StringUtils.hasText(bankDetails.getAccountHolderName());
    }
    return true;
  }

  private boolean validateSecurityAnswer(final List<SecurityAnswer> securityAnswerList) {
    for (final SecurityAnswer securityAnswer : securityAnswerList) {
      if (!StringUtils.hasText(securityAnswer.getAnswerHash())) {
        return false;
      }
    }
    return true;
  }

  private boolean validateUserCredential(final UserCredential userCredential) {
    return StringUtils.hasText(userCredential.getPasswordHash());
  }
}
