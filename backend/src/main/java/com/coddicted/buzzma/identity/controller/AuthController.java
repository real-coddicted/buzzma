package com.coddicted.buzzma.identity.controller;

import com.coddicted.buzzma.identity.dto.SecurityQuestionResponseDto;
import com.coddicted.buzzma.identity.dto.auth.ForgotPasswordLookupRequestDto;
import com.coddicted.buzzma.identity.dto.auth.PasswordResetRequestDto;
import com.coddicted.buzzma.identity.dto.auth.PasswordUpdateRequestDto;
import com.coddicted.buzzma.identity.dto.auth.SignInResult;
import com.coddicted.buzzma.identity.dto.auth.TokensDto;
import com.coddicted.buzzma.identity.dto.auth.UserRegistrationRequestDto;
import com.coddicted.buzzma.identity.dto.auth.UserSignInRequestDto;
import com.coddicted.buzzma.identity.dto.auth.UserSignInResponseDto;
import com.coddicted.buzzma.identity.entity.SecurityQuestionWrapper;
import com.coddicted.buzzma.identity.mapper.AuthMapper;
import com.coddicted.buzzma.identity.mapper.SecurityQuestionMapper;
import com.coddicted.buzzma.identity.service.AuthService;
import com.coddicted.buzzma.shared.exception.UnauthorizedException;
import com.coddicted.buzzma.shared.security.CookieProperties;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import com.coddicted.buzzma.shared.security.JwtProperties;
import com.coddicted.buzzma.shared.turnstile.TurnstileClient;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
public class AuthController {

  private static final String REFRESH_COOKIE_NAME = "refreshToken";
  private static final String REFRESH_COOKIE_PATH = "/api/v1/auth";

  private final AuthService authService;
  private final AuthMapper authMapper;
  private final SecurityQuestionMapper securityQuestionMapper;
  private final JwtProperties jwtProperties;
  private final CookieProperties cookieProperties;
  private final TurnstileClient turnstileClient;

  public AuthController(
      final AuthService authService,
      final AuthMapper authMapper,
      final SecurityQuestionMapper securityQuestionMapper,
      final JwtProperties jwtProperties,
      final CookieProperties cookieProperties,
      final TurnstileClient turnstileClient) {
    this.authService = authService;
    this.authMapper = authMapper;
    this.securityQuestionMapper = securityQuestionMapper;
    this.jwtProperties = jwtProperties;
    this.cookieProperties = cookieProperties;
    this.turnstileClient = turnstileClient;
  }

  @PostMapping("/sign-in")
  public UserSignInResponseDto signIn(
      @Valid @RequestBody final UserSignInRequestDto request, final HttpServletResponse response) {
    this.turnstileClient.verify(request.getCaptchaToken());
    final SignInResult result =
        this.authService.signIn(
            this.authMapper.toUser(request), this.authMapper.toCredential(request));
    setRefreshCookie(response, result.tokens().getRefreshToken());
    return UserSignInResponseDto.builder()
        .tokens(TokensDto.builder().accessToken(result.tokens().getAccessToken()).build())
        .userSummary(this.authMapper.toUserSummary(result.user()))
        .build();
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public void register(@Valid @RequestBody final UserRegistrationRequestDto request) {
    this.turnstileClient.verify(request.getCaptchaToken());
    this.authService.register(
        this.authMapper.toUser(request),
        this.authMapper.toCredential(request),
        this.authMapper.toBankingDetail(request),
        this.authMapper.toSecurityAnswers(request.getSecurityQuestionList()),
        request.getInviteCode(),
        null);
  }

  @PostMapping("/forgot-password/lookup")
  public List<SecurityQuestionResponseDto> forgotPasswordLookup(
      @CurrentUserId final UUID requesterId,
      @Valid @RequestBody final ForgotPasswordLookupRequestDto request) {
    final List<SecurityQuestionWrapper> wrappers =
        this.authService.getSecurityQuestionsByMobile(request.getMobile(), requesterId);
    return this.securityQuestionMapper.toResponseList(wrappers);
  }

  @PostMapping("/password-reset")
  public boolean passwordReset(
      @CurrentUserId final UUID requesterId,
      @Valid @RequestBody final PasswordResetRequestDto request) {
    return this.authService.resetPassword(
        request.getMobile(), request.getNewPassword(), requesterId);
  }

  @PostMapping("/password-update")
  public boolean passwordUpdate(
      @CurrentUserId final UUID requesterId,
      @Valid @RequestBody final PasswordUpdateRequestDto request) {
    return this.authService.updatePassword(
        request.getCurrentPassword(), request.getNewPassword(), requesterId);
  }

  @PostMapping("/refresh")
  public TokensDto refresh(
      @CookieValue(name = REFRESH_COOKIE_NAME, required = false) final String refreshToken,
      final HttpServletResponse response) {
    if (refreshToken == null) {
      throw new UnauthorizedException("No refresh token");
    }
    final TokensDto newTokens = this.authService.refresh(refreshToken);
    setRefreshCookie(response, newTokens.getRefreshToken());
    return TokensDto.builder().accessToken(newTokens.getAccessToken()).build();
  }

  @PostMapping("/sign-out")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void signOut(
      @CookieValue(name = REFRESH_COOKIE_NAME, required = false) final String refreshToken,
      final HttpServletResponse response) {
    try {
      if (refreshToken != null) {
        this.authService.signOut(refreshToken);
      }
    } finally {
      clearRefreshCookie(response);
    }
  }

  private void setRefreshCookie(final HttpServletResponse response, final String rawToken) {
    final ResponseCookie cookie =
        ResponseCookie.from(REFRESH_COOKIE_NAME, rawToken)
            .httpOnly(true)
            .secure(this.cookieProperties.isSecure())
            .sameSite("Strict")
            .path(REFRESH_COOKIE_PATH)
            .maxAge(Duration.ofMillis(this.jwtProperties.getRefreshExpiryMs()))
            .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }

  private void clearRefreshCookie(final HttpServletResponse response) {
    final ResponseCookie cleared =
        ResponseCookie.from(REFRESH_COOKIE_NAME, "")
            .httpOnly(true)
            .secure(this.cookieProperties.isSecure())
            .sameSite("Strict")
            .path(REFRESH_COOKIE_PATH)
            .maxAge(0)
            .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cleared.toString());
  }
}
