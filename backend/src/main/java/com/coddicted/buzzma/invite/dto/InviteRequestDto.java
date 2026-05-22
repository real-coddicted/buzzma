package com.coddicted.buzzma.invite.dto;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class InviteRequestDto {

  int validityInDays;
  int maxUseCount;
}
