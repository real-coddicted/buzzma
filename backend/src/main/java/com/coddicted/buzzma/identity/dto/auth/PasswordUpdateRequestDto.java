package com.coddicted.buzzma.identity.dto.auth;

import com.coddicted.buzzma.shared.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class PasswordUpdateRequestDto {

  @NotBlank String currentPassword;

  @NotBlank @ValidPassword String newPassword;
}
