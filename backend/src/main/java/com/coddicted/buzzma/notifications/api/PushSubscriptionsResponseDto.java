package com.coddicted.buzzma.notifications.api;

import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class PushSubscriptionsResponseDto {

  UUID id;

  UUID userId;

  String app;

  String endpoint;

  @Nullable Integer expirationTime;

  String keysP256dh;

  String keysAuth;

  @Nullable String userAgent;

  Boolean isDeleted;

  @Nullable Instant createdAt;

  @Nullable Instant updatedAt;
}
