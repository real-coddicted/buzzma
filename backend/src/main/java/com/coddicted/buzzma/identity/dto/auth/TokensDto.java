package com.coddicted.buzzma.identity.dto.auth;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class TokensDto {

  String accessToken;
  String refreshToken;
}
