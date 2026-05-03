package com.coddicted.buzzma.identity.service.impl;

import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.Invite;
import com.coddicted.buzzma.identity.entity.SecurityAnswer;
import com.coddicted.buzzma.identity.entity.SecurityQuestionWrapper;
import com.coddicted.buzzma.identity.entity.UserBankingDetail;
import com.coddicted.buzzma.identity.entity.UserCredential;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.service.AuthService;
import com.coddicted.buzzma.identity.service.InviteService;
import com.coddicted.buzzma.identity.service.SecurityQuestionAnswerService;
import com.coddicted.buzzma.identity.service.UserBankingDetailService;
import com.coddicted.buzzma.identity.service.UserCredentialService;
import com.coddicted.buzzma.identity.service.UserService;
import com.coddicted.buzzma.shared.exception.ForbiddenException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final UserCredentialService userCredentialService;
    private final UserBankingDetailService userBankingDetailService;
    private final InviteService inviteService;
    private final SecurityQuestionAnswerService securityQuestionAnswerService;

    public AuthServiceImpl(
            final UserService userService,
            final UserCredentialService userCredentialService,
            final UserBankingDetailService userBankingDetailService,
            final InviteService inviteService,
            final SecurityQuestionAnswerService securityQuestionAnswerService) {
        this.userService = userService;
        this.userCredentialService = userCredentialService;
        this.userBankingDetailService = userBankingDetailService;
        this.inviteService = inviteService;
        this.securityQuestionAnswerService = securityQuestionAnswerService;
    }

    @Override
    public List<SecurityQuestionWrapper> getSecurityQuestionsByMobile(
            final String mobile, final UUID requesterId) {
        final BuzzmaUser existingUser = userService.getByMobile(mobile);
        return securityQuestionAnswerService.getSecurityQuestionsByUserId(existingUser.getId());
    }

    @Override
    public boolean resetPassword(String mobile, String newPassword, UUID requesterId) {
        final BuzzmaUser existingUser = userService.getByMobile(mobile);
        final UserCredential existingUserCredential =
                userCredentialService.getByUserId(existingUser.getId(), requesterId);
        final UserCredential updatedUserCredential =
                existingUserCredential.toBuilder().passwordHash(newPassword).build();
        return userCredentialService.update(updatedUserCredential, requesterId);
    }

    @Override
    @Transactional
    public BuzzmaUser register(final BuzzmaUser user,
                               final UserCredential userCredential,
                               final UserBankingDetail userBankingDetail,
                               final List<SecurityAnswer> securityAnswerList,
                               final Invite invite,
                               final UUID requesterId) {
        // can register
        if (canRegister(user, userCredential, userBankingDetail, securityAnswerList, invite)) {

            // Save user
            final BuzzmaUser savedUser = userService.create(user);
            // Save User Credential
            userCredentialService.create(
                    userCredential.toBuilder().userId(savedUser.getId()).build(), requesterId);
            // Save user banking detail
            final UserBankingDetail savedUserBankingDetail = userBankingDetailService.create(userBankingDetail, requesterId);
            // Save security answer
            securityAnswerList.forEach(securityQuestionAnswerService::createSecurityAnswer);
            // Consume invite
            inviteService.consume(invite, requesterId);
            return savedUser;
        }


        return null;
    }

    @Override
    public BuzzmaUser signIn(final BuzzmaUser user, final UserCredential userCredential) {
        final BuzzmaUser existingUser = userService.getByMobile(user.getMobile());
        if (!userCredentialService.verify(existingUser.getId(), userCredential.getPasswordHash())) {
            throw new ForbiddenException("Invalid credentials");
        }
        return existingUser;
    }

    @Override
    public BuzzmaUser refresh(final String refreshToken) {
        final UUID userId = jwtService.validateRefreshToken(refreshToken);
        return userService.getById(userId);
    }

    private boolean canRegister(final BuzzmaUser user,
                                final UserCredential userCredential,
                                final UserBankingDetail userBankingDetail,
                                final List<SecurityAnswer> securityAnswerList,
                                final Invite invite) {
        boolean validUser = validateUser(user);
        boolean validBankingDetails = validateUserBankingDetails(user, userBankingDetail);
        boolean validInvite = inviteService.verify(user.getRole(), invite.getCode());
        boolean validSecurityAnswerList = validateSecurityAnswer(securityAnswerList);
        boolean validPassword = validateUserCredential(userCredential);
        return validUser && validBankingDetails && validSecurityAnswerList && validInvite && validPassword;

    }

    private boolean validateUser(final BuzzmaUser user) {
        return !userService.existsByMobile(user.getMobile());
    }

    private boolean validateUserBankingDetails(final BuzzmaUser user, final UserBankingDetail
            userBankingDetail) {
        if (user.getRole() == UserRole.ROLE_BUYER) {
            return StringUtils.hasText(userBankingDetail.getBankName())
                    && StringUtils.hasText(userBankingDetail.getAccountNumber())
                    && StringUtils.hasText(userBankingDetail.getBankIfscCode())
                    && StringUtils.hasText(userBankingDetail.getAccountHolderName());
        }
        return true;
    }

    private boolean validateSecurityAnswer(List<SecurityAnswer> securityAnswerList) {
        for (SecurityAnswer securityAnswer : securityAnswerList) {
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
