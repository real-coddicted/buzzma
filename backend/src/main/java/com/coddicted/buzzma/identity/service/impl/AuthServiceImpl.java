// package com.coddicted.buzzma.identity.service.impl;
//
// import com.coddicted.buzzma.identity.entity.*;
// import com.coddicted.buzzma.identity.service.AuthService;
// import com.coddicted.buzzma.identity.service.InviteService;
// import com.coddicted.buzzma.identity.service.SecurityQuestionAnswerService;
// import com.coddicted.buzzma.identity.service.UserService;
// import com.coddicted.buzzma.shared.common.PasswordService;
// import com.coddicted.buzzma.shared.security.JwtService;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;
// import org.springframework.util.StringUtils;
//
// import java.util.UUID;
//
// @Service
// public class AuthServiceImpl implements AuthService {
//
//
//    private final UserService userService;
//    private final SecurityQuestionAnswerService securityQuestionAnswerService;
//    private final PasswordService passwordService;
//    private final JwtService jwtService;
//    private final InviteService inviteService;
//
//    public AuthServiceImpl(
//            JwtService jwtService,
//            PasswordService passwordService,
//            UserService userService,
//            InviteService inviteService,
//            SecurityQuestionAnswerService securityQuestionAnswerService) {
//        this.jwtService = jwtService;
//        this.passwordService = passwordService;
//        this.userService = userService;
//        this.securityQuestionAnswerService = securityQuestionAnswerService;
//        this.inviteService = inviteService;
//    }
//
//    @Override
//    @Transactional
//    public BuzzmaUser register(final BuzzmaUser user, final UserBankingDetails userBankingDetails,
// final SecurityAnswer securityAnswer, final Invite invite, final UUID requesterId) {
//        // Check mobile uniqueness
//        if (canRegister(user, userBankingDetails, securityAnswer, invite, requesterId)) {
//            // Hash password and security answer
//            final String hashedPassword = passwordService.hashPassword(user.getPassword());
//            final String hashedAnswer =
// passwordService.hashPassword(securityAnswer.getAnswerHash());
//
//            // Save user
//            final BuzzmaUser savedUser = userService.create(user.toBuilder()
//                    .password(hashedPassword)
//                    .build());
//
//            // Save banking details if buyer
//            if (user.getRole() == UserRole.ROLE_BUYER) {
//                userBankingDetails.setUserId(savedUser.getId());
//                userService.saveUserBankingDetails(userBankingDetails);
//            }
//
//            // Save security answer
//            securityAnswer.setUserId(savedUser.getId());
//            securityAnswer.setAnswerHash(hashedAnswer);
//            securityQuestionAnswerService.saveSecurityAnswer(securityAnswer);
//
//            // Consume invite
//            inviteService.consume(user.getRole(), invite.getCode(), requesterId);
//
//            return savedUser;
//        }
//
//
//        return null;
//    }
//
//    private boolean canRegister(final BuzzmaUser user, final UserBankingDetails
// userBankingDetails, final SecurityAnswer securityAnswer, final Invite invite, final UUID
// requesterId) {
//        boolean mobileNumberUnique = !userService.existsByMobile(user.getMobile());
//        boolean validBankingDetails = verifyUserBankingDetails(user, userBankingDetails);
//        boolean inviteValid = inviteService.verify(user.getRole(), invite.getCode(), requesterId);
//        return mobileNumberUnique && validBankingDetails && inviteValid;
//
//    }
//
//    private boolean verifyUserBankingDetails(final BuzzmaUser user, final UserBankingDetails
// userBankingDetails) {
//        if (user.getRole() == UserRole.ROLE_BUYER) {
//            return StringUtils.hasText(userBankingDetails.getBankName())
//                    && StringUtils.hasText(userBankingDetails.getAccountNumber())
//                    && StringUtils.hasText(userBankingDetails.getBankIfscCode())
//                    && StringUtils.hasText(userBankingDetails.getAccountHolderName());
//        }
//        return true;
//    }
//
//    private boolean verifySecurityAnswer(SecurityAnswer securityAnswer) {
//        return StringUtils.hasText(securityAnswer.getAnswerHash());
//    }
//
//    private boolean verifyPassword(BuzzmaUser user){
//        return StringUtils.hasText(user.getPassword());
//    }
//
//
// }
