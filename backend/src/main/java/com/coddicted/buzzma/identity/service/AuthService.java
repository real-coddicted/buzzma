package com.coddicted.buzzma.identity.service;

import com.coddicted.buzzma.identity.api.auth.ForgotPasswordLookupRequest;
import com.coddicted.buzzma.identity.api.auth.ForgotPasswordResetRequest;
import com.coddicted.buzzma.identity.api.auth.LoginRequest;
import com.coddicted.buzzma.identity.api.auth.LoginResponse;
import com.coddicted.buzzma.identity.api.auth.RegisterBrandRequest;
import com.coddicted.buzzma.identity.api.auth.RegisterOpsRequest;
import com.coddicted.buzzma.identity.api.auth.RegisterRequest;
import com.coddicted.buzzma.identity.api.auth.SecurityQuestionsRequest;
import com.coddicted.buzzma.identity.api.auth.UpdateProfileRequest;
import com.coddicted.buzzma.identity.api.auth.UserSummary;
import java.util.UUID;

public interface AuthService {

  LoginResponse register(RegisterRequest request);

  LoginResponse login(LoginRequest request);

  LoginResponse refresh(String refreshToken);

  UserSummary me(UUID userId);

  LoginResponse registerOps(RegisterOpsRequest request);

  LoginResponse registerBrand(RegisterBrandRequest request);

  UserSummary updateProfile(UUID userId, UpdateProfileRequest request);

  void saveSecurityQuestions(UUID userId, SecurityQuestionsRequest request);

  UserSummary forgotPasswordLookup(ForgotPasswordLookupRequest request);

  void forgotPasswordReset(ForgotPasswordResetRequest request);
}
