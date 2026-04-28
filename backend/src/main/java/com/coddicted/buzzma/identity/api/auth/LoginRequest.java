package com.coddicted.buzzma.identity.api.auth;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class LoginRequest {

  String mobile;

  String username;

  @Size(min = 8, max = 200)
  String password;
}
