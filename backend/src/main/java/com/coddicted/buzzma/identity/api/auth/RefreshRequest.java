package com.coddicted.buzzma.identity.api.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class RefreshRequest {
  @NotBlank String refreshToken;
}
