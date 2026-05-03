package com.coddicted.buzzma.support.api;

import jakarta.annotation.Nullable;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class TicketRequestDto {

  UUID userId;

  @Nullable String userName;

  @Nullable String role;

  @Nullable String orderId;

  @Nullable String issueType;

  @Nullable String description;

  @Nullable String status;

  @Nullable String targetRole;

  @Nullable String priority;

  @Nullable UUID resolvedBy;

  @Nullable String resolutionNote;
}
