package com.coddicted.buzzma.identity.controller;

import com.coddicted.buzzma.identity.api.SecurityQuestionResponseDto;
import com.coddicted.buzzma.identity.api.auth.ForgotPasswordLookupRequestDto;
import com.coddicted.buzzma.identity.api.auth.PasswordResetRequestDto;
import com.coddicted.buzzma.identity.api.auth.RefreshTokenRequestDto;
import com.coddicted.buzzma.identity.api.auth.TokensDto;
import com.coddicted.buzzma.identity.api.auth.UserRegistrationRequestDto;
import com.coddicted.buzzma.identity.api.auth.UserSignInRequestDto;
import com.coddicted.buzzma.identity.api.auth.UserSignInResponseDto;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.SecurityQuestionWrapper;
import com.coddicted.buzzma.identity.mapper.AuthMapper;
import com.coddicted.buzzma.identity.mapper.SecurityQuestionMapper;
import com.coddicted.buzzma.identity.service.AuthService;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import com.coddicted.buzzma.shared.security.JwtService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
public class AuthController {

  private final AuthService authService;
  private final AuthMapper authMapper;
  private final SecurityQuestionMapper securityQuestionMapper;
  private final JwtService jwtService;

  public AuthController(
      final AuthService authService,
      final AuthMapper authMapper,
      final SecurityQuestionMapper securityQuestionMapper,
      final JwtService jwtService) {
    this.authService = authService;
    this.authMapper = authMapper;
    this.securityQuestionMapper = securityQuestionMapper;
    this.jwtService = jwtService;
  }

  @PostMapping("/sign-in")
  public UserSignInResponseDto signIn(@Valid @RequestBody final UserSignInRequestDto request) {
    final BuzzmaUser user = authService.signIn(authMapper.toUser(request), authMapper.toCredential(request));
    return buildUserSignInResponse(user);
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public void register(@Valid @RequestBody final UserRegistrationRequestDto request) {
    authService.register(
        authMapper.toUser(request),
        authMapper.toCredential(request),
        authMapper.toBankingDetail(request),
        authMapper.toSecurityAnswers(request.getSecurityQuestionList()),
        authMapper.toInvite(request),
        null);
  }

  @PostMapping("/forgot-password/lookup")
  public List<SecurityQuestionResponseDto> forgotPasswordLookup(
      @CurrentUserId final UUID requesterId,
      @Valid @RequestBody final ForgotPasswordLookupRequestDto request) {
    final List<SecurityQuestionWrapper> wrappers =
        authService.getSecurityQuestionsByMobile(request.getMobile(), requesterId);
    return securityQuestionMapper.toResponseList(wrappers);
  }

  @PostMapping("/password-reset")
  public boolean passwordReset(
      @CurrentUserId final UUID requesterId,
      @Valid @RequestBody final PasswordResetRequestDto request) {
    return authService.resetPassword(request.getMobile(), request.getNewPassword(), requesterId);
  }

  @PostMapping("/refresh")
  public TokensDto refresh(@Valid @RequestBody final RefreshTokenRequestDto request) {
    final BuzzmaUser user = authService.refresh(request.getRefreshToken());
    return buildTokens(user);
  }

  private UserSignInResponseDto buildUserSignInResponse(final BuzzmaUser user) {
    return UserSignInResponseDto.builder()
        .tokens(buildTokens(user))
        .userSummary(authMapper.toUserSummary(user))
        .build();
  }

  private TokensDto buildTokens(final BuzzmaUser user) {
    return TokensDto.builder()
        .accessToken(jwtService.generateAccessToken(user.getId()))
        .refreshToken(jwtService.generateRefreshToken(user.getId()))
        .build();
  }
}
