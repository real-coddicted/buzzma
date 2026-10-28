package com.coddicted.buzzma.identity.dto.auth;

import com.coddicted.buzzma.shared.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
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

  @NotNull UUID questionId;

  @NotBlank
  @Size(max = 200)
  String answer;

  @NotBlank @ValidPassword String newPassword;
}
