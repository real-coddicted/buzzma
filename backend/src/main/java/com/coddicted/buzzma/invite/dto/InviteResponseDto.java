package com.coddicted.buzzma.invite.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class InviteResponseDto {

  String code;

  String status;

  int validTo;

  int maxUseCount;

  int usedCount;

  UUID createdBy;

  Instant createdAt;

  UUID updatedBy;

  Instant updatedAt;
}
