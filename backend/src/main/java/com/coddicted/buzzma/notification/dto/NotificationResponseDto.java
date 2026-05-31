package com.coddicted.buzzma.notification.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class NotificationResponseDto {
  UUID id;
  UUID userId;
  String status;
  String title;
  String message;
  boolean isPinned;
  Instant createdAt;
  Instant updatedAt;
}
