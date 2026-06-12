package com.coddicted.buzzma.identity.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class PasswordUpdateRequestDto {

  @NotBlank
  @Size(min = 8, max = 200)
  String currentPassword;

  @NotBlank
  @Size(min = 8, max = 200)
  String newPassword;
}
