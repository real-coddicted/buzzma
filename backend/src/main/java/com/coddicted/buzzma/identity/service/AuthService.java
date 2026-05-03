package com.coddicted.buzzma.identity.service;

import com.coddicted.buzzma.identity.entity.*;
import java.util.List;
import java.util.UUID;

public interface AuthService {

  //  BuzzmaUser register(
  //      BuzzmaUser user,
  //      UserCredential userCredential,
  //      UserBankingDetails userBankingDetails,
  //      SecurityAnswer securityAnswer,
  //      Invite invite,
  //      UUID requesterId);

  List<SecurityQuestionWrapper> getSecurityQuestionsByMobile(String mobile, UUID requesterId);

  boolean resetPassword(String mobile, String newPassword, UUID requesterId);
}
