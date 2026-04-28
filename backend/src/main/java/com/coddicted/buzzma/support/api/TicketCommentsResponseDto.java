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
public class TicketCommentsResponseDto {

  UUID id;

  UUID ticketId;

  UUID userId;

  String userName;

  String role;

  String message;

  Boolean isDeleted;

  @Nullable Instant createdAt;
}
