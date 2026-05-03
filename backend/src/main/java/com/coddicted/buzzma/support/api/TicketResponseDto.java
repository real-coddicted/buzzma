package com.coddicted.buzzma.support.api;

import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class TicketResponseDto {

  UUID id;

  UUID userId;

  String userName;

  String role;

  @Nullable String orderId;

  String issueType;

  String description;

  String status;

  @Nullable String targetRole;

  @Nullable String priority;

  @Nullable UUID resolvedBy;

  @Nullable Instant resolvedAt;

  @Nullable String resolutionNote;

  Boolean isDeleted;

  @Nullable Instant createdAt;

  @Nullable Instant updatedAt;
}
