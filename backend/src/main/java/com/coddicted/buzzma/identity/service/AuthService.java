package com.coddicted.buzzma.identity.service;

import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.Invite;
import com.coddicted.buzzma.identity.entity.SecurityAnswer;
import com.coddicted.buzzma.identity.entity.UserBankingDetails;
import com.coddicted.buzzma.identity.entity.UserCredential;
import java.util.UUID;

public interface AuthService {

  BuzzmaUser register(
      BuzzmaUser user,
      UserCredential userCredential,
      UserBankingDetails userBankingDetails,
      SecurityAnswer securityAnswer,
      Invite invite,
      UUID requesterId);

  //  LoginResponse login(LoginRequest request);
  //
  //  LoginResponse refresh(String refreshToken);
  //
  //  UserSummary me(UUID userId);
  //
  //  LoginResponse registerOps(RegisterOpsRequest request);
  //
  //  LoginResponse registerBrand(RegisterBrandRequest request);
  //
  //  UserSummary updateProfile(UUID userId, UpdateProfileRequest request);
  //
  //  void saveSecurityQuestions(UUID userId, SecurityQuestionsRequest request);
  //
  //  UserSummary forgotPasswordLookup(ForgotPasswordLookupRequest request);
  //
  //  void forgotPasswordReset(ForgotPasswordResetRequest request);
}
