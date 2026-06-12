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
import com.coddicted.buzzma.invite.entity.InviteStatus;
import com.coddicted.buzzma.invite.service.InviteService;
import com.coddicted.buzzma.settings.entity.UserSettings;
import com.coddicted.buzzma.settings.service.UserSettingsService;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.exception.ForbiddenException;
import com.coddicted.buzzma.shared.exception.PasswordMatchException;
import com.coddicted.buzzma.shared.security.JwtService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthServiceImpl implements AuthService {

  private static final Map<UserRole, Set<UserRole>> ALLOWED_ROLES_BY_INVITER =
      Map.of(
          UserRole.ROLE_ADMIN, Set.of(UserRole.ROLE_BRAND, UserRole.ROLE_AGENCY),
          UserRole.ROLE_BRAND, Set.of(UserRole.ROLE_AGENCY),
          UserRole.ROLE_AGENCY, Set.of(UserRole.ROLE_MEDIATOR),
          UserRole.ROLE_MEDIATOR, Set.of(UserRole.ROLE_BUYER));

  private final JwtService jwtService;
  private final UserService userService;
  private final UserCredentialService userCredentialService;
  private final UserBankingDetailService userBankingDetailService;
  private final InviteService inviteService;
  private final SecurityQuestionAnswerService securityQuestionAnswerService;
  private final ConnectionService connectionService;
  private final UserSettingsService userSettingsService;

  public AuthServiceImpl(
      final JwtService jwtService,
      final UserService userService,
      final UserCredentialService userCredentialService,
      final UserBankingDetailService userBankingDetailService,
      final InviteService inviteService,
      final SecurityQuestionAnswerService securityQuestionAnswerService,
      final ConnectionService connectionService,
      final UserSettingsService userSettingsService) {
    this.jwtService = jwtService;
    this.userService = userService;
    this.userCredentialService = userCredentialService;
    this.userBankingDetailService = userBankingDetailService;
    this.inviteService = inviteService;
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
    final Invite invite = this.inviteService.getByCode(inviteCode);
    if (canRegister(user, userCredential, userBankingDetail, securityAnswerList, invite)) {
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
      // consume invite
      this.inviteService.consume(invite, requesterId);

      // create connection between inviter and new user
      this.connectionService.createConnection(
          Connection.builder()
              .fromUserId(invite.getOwnerId())
              .toUserId(savedUser.getId())
              .status(ConnectionStatus.CONNECTION_STATUS_REQUESTED)
              .build());

      // build and save userSettings with default settings based on user role
      final UserSettings userSettings =
          this.userSettingsService.getDefaultSettingsByUserRole(user.getRole()).toBuilder()
              .userId(savedUser.getId())
              .build();

      this.userSettingsService.create(userSettings, requesterId);

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
      final List<SecurityAnswer> securityAnswerList,
      final Invite invite) {
    final boolean validUser = validateUser(user);
    final boolean validBankingDetails = validateUserBankingDetails(user, userBankingDetail);
    final boolean validInvite =
        invite.getStatus() == InviteStatus.INVITE_STATUS_ACTIVE
            && invite.getUsedCount() < invite.getMaxUseCount();
    validateRoleForInvite(user.getRole(), invite);
    final boolean validSecurityAnswerList = validateSecurityAnswer(securityAnswerList);
    final boolean validPassword = validateUserCredential(userCredential);
    return validUser
        && validBankingDetails
        && validSecurityAnswerList
        && validInvite
        && validPassword;
  }

  private void validateRoleForInvite(final UserRole requestedRole, final Invite invite) {
    final BuzzmaUser owner = this.userService.getById(invite.getOwnerId());
    final Set<UserRole> allowed = ALLOWED_ROLES_BY_INVITER.getOrDefault(owner.getRole(), Set.of());
    if (!allowed.contains(requestedRole)) {
      throw new BusinessRuleViolationException(
          "Role " + requestedRole + " is not permitted for invites issued by " + owner.getRole());
    }
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
