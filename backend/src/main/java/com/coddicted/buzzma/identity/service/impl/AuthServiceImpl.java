package com.coddicted.buzzma.identity.service.impl;

import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.SecurityQuestionWrapper;
import com.coddicted.buzzma.identity.entity.UserCredential;
import com.coddicted.buzzma.identity.service.AuthService;
import com.coddicted.buzzma.identity.service.SecurityQuestionAnswerService;
import com.coddicted.buzzma.identity.service.UserCredentialService;
import com.coddicted.buzzma.identity.service.UserService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

  private final UserService userService;
  private final UserCredentialService userCredentialService;
  private final SecurityQuestionAnswerService securityQuestionAnswerService;

  public AuthServiceImpl(
      final UserService userService,
      final UserCredentialService userCredentialService,
      final SecurityQuestionAnswerService securityQuestionAnswerService) {
    this.userService = userService;
    this.userCredentialService = userCredentialService;
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

  //    @Override
  //    @Transactional
  //    public BuzzmaUser register(final BuzzmaUser user, final UserCredential userCredential,final
  // UserBankingDetails userBankingDetails,
  //                               final SecurityAnswer securityAnswer, final
  //                                   Invite invite, final UUID requesterId) {
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
  //        boolean inviteValid = inviteService.verify(user.getRole(), invite.getCode(),
  // requesterId);
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

}
