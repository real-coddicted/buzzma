package com.coddicted.buzzma.identity.api;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class InvitesRequestDto {

  @NotBlank String code;

  @NotBlank String role;

  @Nullable String label;

  @Nullable UUID parentUserId;

  @Nullable String parentCode;

  @Nullable String status;

  @Nullable Integer maxUses;

  @Nullable Instant expiresAt;

  @Nullable String uses;

  @Nullable UUID revokedBy;

  @Nullable Instant revokedAt;
}
