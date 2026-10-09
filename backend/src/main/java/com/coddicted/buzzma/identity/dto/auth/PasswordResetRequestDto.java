package com.coddicted.buzzma.identity.dto.auth;

import com.coddicted.buzzma.shared.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class PasswordResetRequestDto {

  @NotBlank
  @Size(min = 10, max = 10)
  String mobile;

  @NotBlank @ValidPassword String newPassword;
}
