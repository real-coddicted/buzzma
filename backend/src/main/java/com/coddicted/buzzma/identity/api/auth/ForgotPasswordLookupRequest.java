package com.coddicted.buzzma.identity.api.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ForgotPasswordLookupRequest {

  @NotBlank
  @Size(min = 10, max = 10)
  String mobile;
}
