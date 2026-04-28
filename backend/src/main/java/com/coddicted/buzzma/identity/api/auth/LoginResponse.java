package com.coddicted.buzzma.identity.api.auth;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LoginResponse {

  Tokens tokens;

  UserSummary user;

  @Value
  @Builder
  public static class Tokens {
    String accessToken;
    String refreshToken;
  }
}
