package com.coddicted.buzzma.notifications.api;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class PushSubscriptionsRequestDto {

  UUID userId;

  @NotBlank String app;

  @NotBlank String endpoint;

  @Nullable Integer expirationTime;

  @NotBlank String keysP256dh;

  @NotBlank String keysAuth;

  @Nullable String userAgent;
}
