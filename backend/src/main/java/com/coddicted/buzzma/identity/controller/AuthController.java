package com.coddicted.buzzma.identity.controller;

import com.coddicted.buzzma.identity.api.SecurityQuestionResponseDto;
import com.coddicted.buzzma.identity.api.auth.ForgotPasswordLookupRequestDto;
import com.coddicted.buzzma.identity.api.auth.PasswordResetRequestDto;
import com.coddicted.buzzma.identity.entity.SecurityQuestionWrapper;
import com.coddicted.buzzma.identity.mapper.SecurityQuestionMapper;
import com.coddicted.buzzma.identity.service.AuthService;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
public class AuthController {
  private final AuthService authService;
  private final SecurityQuestionMapper securityQuestionMapper;

  public AuthController(
      final AuthService authService, SecurityQuestionMapper securityQuestionMapper) {
    this.authService = authService;
    this.securityQuestionMapper = securityQuestionMapper;
  }

  @PostMapping("/forgot-password/lookup")
  public List<SecurityQuestionResponseDto> forgotPassword(
      @CurrentUserId final UUID requesterId,
      @Valid @RequestBody final ForgotPasswordLookupRequestDto request) {
    final List<SecurityQuestionWrapper> securityQuestionWrapperList =
        authService.getSecurityQuestionsByMobile(request.getMobile(), requesterId);
    return securityQuestionMapper.toResponseList(securityQuestionWrapperList);
  }

  @PostMapping("password-reset")
  public boolean passwordReset(
      @CurrentUserId final UUID requesterId,
      @Valid @RequestBody final PasswordResetRequestDto request) {
    return authService.resetPassword(request.getMobile(), request.getNewPassword(), requesterId);
  }
}
