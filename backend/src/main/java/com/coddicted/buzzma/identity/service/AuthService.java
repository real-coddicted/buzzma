package com.coddicted.buzzma.identity.service;

import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.Invite;
import com.coddicted.buzzma.identity.entity.SecurityAnswer;
import com.coddicted.buzzma.identity.entity.SecurityQuestionWrapper;
import com.coddicted.buzzma.identity.entity.UserBankingDetail;
import com.coddicted.buzzma.identity.entity.UserCredential;
import java.util.List;
import java.util.UUID;

public interface AuthService {

  BuzzmaUser register(
      BuzzmaUser user,
      UserCredential userCredential,
      UserBankingDetail userBankingDetail,
      List<SecurityAnswer> securityAnswerList,
      Invite invite,
      UUID requesterId);

  BuzzmaUser signIn(BuzzmaUser user, UserCredential userCredential);

  List<SecurityQuestionWrapper> getSecurityQuestionsByMobile(String mobile, UUID requesterId);

  boolean resetPassword(String mobile, String newPassword, UUID requesterId);

  BuzzmaUser refresh(String refreshToken);
}
