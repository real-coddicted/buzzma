package com.coddicted.buzzma.identity.api.auth;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class UserSignInResponseDto {

  TokensDto tokens;
  UserSummary userSummary;
}
