package com.coddicted.buzzma.identity.dto;

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

  String role;

  String status;

  int validTo;

  UUID createdBy;

  Instant createdAt;

  UUID updatedBy;

  Instant updatedAt;
}
