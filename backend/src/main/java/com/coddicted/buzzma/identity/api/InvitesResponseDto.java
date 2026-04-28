package com.coddicted.buzzma.identity.api;

import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class InvitesResponseDto {

  UUID id;

  String code;

  String role;

  @Nullable String label;

  @Nullable UUID parentUserId;

  @Nullable String parentCode;

  String status;

  Integer maxUses;

  Integer useCount;

  @Nullable Instant expiresAt;

  @Nullable UUID createdBy;

  @Nullable UUID usedBy;

  @Nullable Instant usedAt;

  @Nullable String uses;

  @Nullable UUID revokedBy;

  @Nullable Instant revokedAt;

  @Nullable Instant createdAt;

  @Nullable Instant updatedAt;
}
