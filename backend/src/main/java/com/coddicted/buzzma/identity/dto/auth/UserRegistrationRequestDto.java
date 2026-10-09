package com.coddicted.buzzma.identity.dto.auth;

import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.shared.validation.ValidPassword;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class UserRegistrationRequestDto {

  @NotBlank
  @Size(min = 2, max = 120)
  String name;

  @NotBlank
  @Size(min = 10, max = 10)
  String mobile;

  @NotBlank @Email String email;

  @NotBlank @ValidPassword String password;

  List<SecurityQuestionWrapper> securityQuestionList;

  String inviteCode;

  UserRole userRole;

  @Nullable String bankAccountNumber;

  @Nullable String bankIfscCode;

  @Nullable String bankName;

  @Nullable String bankAccountHolderName;

  String captchaToken;

  @AssertTrue(message = "Terms and conditions must be accepted")
  boolean termsAccepted;
}
