package com.coddicted.buzzma.identity.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class VerifyEmailOtpRequestDto {

  @Pattern(regexp = "\\d{6}", message = "code must be a 6-digit number")
  String code;
}
