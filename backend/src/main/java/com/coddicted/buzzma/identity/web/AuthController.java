package com.coddicted.buzzma.identity.web;

import com.coddicted.buzzma.identity.api.auth.ForgotPasswordLookupRequest;
import com.coddicted.buzzma.identity.api.auth.ForgotPasswordResetRequest;
import com.coddicted.buzzma.identity.api.auth.LoginRequest;
import com.coddicted.buzzma.identity.api.auth.LoginResponse;
import com.coddicted.buzzma.identity.api.auth.RefreshRequest;
import com.coddicted.buzzma.identity.api.auth.RegisterBrandRequest;
import com.coddicted.buzzma.identity.api.auth.RegisterOpsRequest;
import com.coddicted.buzzma.identity.api.auth.RegisterRequest;
import com.coddicted.buzzma.identity.api.auth.SecurityQuestionsRequest;
import com.coddicted.buzzma.identity.api.auth.UpdateProfileRequest;
import com.coddicted.buzzma.identity.api.auth.UserSummary;
import com.coddicted.buzzma.identity.service.AuthService;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public LoginResponse register(@Valid @RequestBody RegisterRequest request) {
    return authService.register(request);
  }

  @PostMapping("/login")
  public LoginResponse login(@Valid @RequestBody LoginRequest request) {
    return authService.login(request);
  }

  @PostMapping("/refresh")
  public LoginResponse refresh(@Valid @RequestBody RefreshRequest request) {
    return authService.refresh(request.getRefreshToken());
  }

  @GetMapping("/me")
  @PreAuthorize("isAuthenticated()")
  public UserSummary me(@CurrentUserId UUID userId) {
    return authService.me(userId);
  }

  @PostMapping("/register-ops")
  @ResponseStatus(HttpStatus.CREATED)
  public LoginResponse registerOps(@Valid @RequestBody RegisterOpsRequest request) {
    return authService.registerOps(request);
  }

  @PostMapping("/register-brand")
  @ResponseStatus(HttpStatus.CREATED)
  public LoginResponse registerBrand(@Valid @RequestBody RegisterBrandRequest request) {
    return authService.registerBrand(request);
  }

  @PatchMapping("/profile")
  @PreAuthorize("isAuthenticated()")
  public UserSummary updateProfile(
      @CurrentUserId UUID userId, @Valid @RequestBody UpdateProfileRequest request) {
    return authService.updateProfile(userId, request);
  }

  @PostMapping("/security-questions")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Void> saveSecurityQuestions(
      @CurrentUserId UUID userId, @Valid @RequestBody SecurityQuestionsRequest request) {
    authService.saveSecurityQuestions(userId, request);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/forgot-password/lookup")
  public UserSummary forgotPasswordLookup(@Valid @RequestBody ForgotPasswordLookupRequest request) {
    return authService.forgotPasswordLookup(request);
  }

  @PostMapping("/forgot-password/reset")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public ResponseEntity<Void> forgotPasswordReset(
      @Valid @RequestBody ForgotPasswordResetRequest request) {
    authService.forgotPasswordReset(request);
    return ResponseEntity.noContent().build();
  }
}
