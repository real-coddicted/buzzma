package com.coddicted.buzzma.identity.dto;

import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.entity.UserStatus;
import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class UserSummaryDto {

  UUID id;

  String name;

  String mobile;

  @Nullable String email;

  UserRole role;

  UserStatus status;

  String code;

  @Nullable String avatar;

  Instant createdAt;

  @Nullable String createdBy;

  Instant updatedAt;

  @Nullable String updatedBy;
}
