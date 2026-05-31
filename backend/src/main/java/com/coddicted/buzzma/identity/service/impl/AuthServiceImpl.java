package com.coddicted.buzzma.identity.service.impl;

import com.coddicted.buzzma.connection.entity.Connection;
import com.coddicted.buzzma.connection.entity.ConnectionStatus;
import com.coddicted.buzzma.connection.service.ConnectionService;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.SecurityAnswer;
import com.coddicted.buzzma.identity.entity.SecurityQuestionWrapper;
import com.coddicted.buzzma.identity.entity.UserBankingDetail;
import com.coddicted.buzzma.identity.entity.UserCredential;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.entity.UserStatus;
import com.coddicted.buzzma.identity.service.AuthService;
import com.coddicted.buzzma.identity.service.SecurityQuestionAnswerService;
import com.coddicted.buzzma.identity.service.UserBankingDetailService;
import com.coddicted.buzzma.identity.service.UserCredentialService;
import com.coddicted.buzzma.identity.service.UserService;
import com.coddicted.buzzma.invite.entity.Invite;
import com.coddicted.buzzma.invite.service.InviteService;
import com.coddicted.buzzma.shared.exception.ForbiddenException;
import com.coddicted.buzzma.shared.security.JwtService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthServiceImpl implements AuthService {

  private final JwtService jwtService;
  private final UserService userService;
  private final UserCredentialService userCredentialService;
  private final UserBankingDetailService userBankingDetailService;
  private final InviteService inviteService;
  private final SecurityQuestionAnswerService securityQuestionAnswerService;
  private final ConnectionService connectionService;

  public AuthServiceImpl(
      final JwtService jwtService,
      final UserService userService,
      final UserCredentialService userCredentialService,
      final UserBankingDetailService userBankingDetailService,
      final InviteService inviteService,
      final SecurityQuestionAnswerService securityQuestionAnswerService,
      final ConnectionService connectionService) {
    this.jwtService = jwtService;
    this.userService = userService;
    this.userCredentialService = userCredentialService;
    this.userBankingDetailService = userBankingDetailService;
    this.inviteService = inviteService;
    this.securityQuestionAnswerService = securityQuestionAnswerService;
    this.connectionService = connectionService;
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
    // can register
    if (canRegister(user, userCredential, userBankingDetail, securityAnswerList, inviteCode)) {

      // Save user
      user.setStatus(UserStatus.USER_STATUS_ACTIVE);
      final BuzzmaUser savedUser = this.userService.create(user);
      // Save User Credential
      this.userCredentialService.create(
          userCredential.toBuilder().userId(savedUser.getId()).build(), requesterId);
      // Save security answer
      securityAnswerList.forEach(
          securityAnswer -> {
            securityAnswer.setUserId(savedUser.getId());
            this.securityQuestionAnswerService.createSecurityAnswer(securityAnswer);
          });
      // Consume invite
      final Invite invite = this.inviteService.getByCode(inviteCode);
      this.inviteService.consume(invite, requesterId);

      // create connection request
      this.connectionService.createConnection(
          Connection.builder()
              .fromUserId(invite.getOwnerId())
              .toUserId(savedUser.getId())
              .status(ConnectionStatus.CONNECTION_STATUS_REQUESTED)
              .build());
      return savedUser;
    }

    return null;
  }

  @Override
  public BuzzmaUser signIn(final BuzzmaUser user, final UserCredential userCredential) {
    final BuzzmaUser existingUser = this.userService.getByMobile(user.getMobile());
    if (!this.userCredentialService.verify(
        existingUser.getId(), userCredential.getPasswordHash())) {
      throw new ForbiddenException("Invalid credentials");
    }
    return existingUser;
  }

  @Override
  public BuzzmaUser refresh(final String refreshToken) {
    final UUID userId = this.jwtService.validateRefreshToken(refreshToken);
    return this.userService.getById(userId);
  }

  private boolean canRegister(
      final BuzzmaUser user,
      final UserCredential userCredential,
      final UserBankingDetail userBankingDetail,
      final List<SecurityAnswer> securityAnswerList,
      final String inviteCode) {
    final boolean validUser = validateUser(user);
    final boolean validBankingDetails = validateUserBankingDetails(user, userBankingDetail);
    final boolean validInvite = this.inviteService.verify(inviteCode);
    final boolean validSecurityAnswerList = validateSecurityAnswer(securityAnswerList);
    final boolean validPassword = validateUserCredential(userCredential);
    return validUser
        && validBankingDetails
        && validSecurityAnswerList
        && validInvite
        && validPassword;
  }

  private boolean validateUser(final BuzzmaUser user) {
    return !this.userService.existsByMobile(user.getMobile());
  }

  private boolean validateUserBankingDetails(
      final BuzzmaUser user, final UserBankingDetail userBankingDetail) {
    if (user.getRole() == UserRole.ROLE_BUYER) {
      return StringUtils.hasText(userBankingDetail.getBankName())
          && StringUtils.hasText(userBankingDetail.getAccountNumber())
          && StringUtils.hasText(userBankingDetail.getBankIfscCode())
          && StringUtils.hasText(userBankingDetail.getAccountHolderName());
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
