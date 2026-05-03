package com.coddicted.buzzma.identity.api;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ConsumeInviteRequestDto {
  @NotBlank String inviteCode;
  @NotBlank String inviteeRole;
}
