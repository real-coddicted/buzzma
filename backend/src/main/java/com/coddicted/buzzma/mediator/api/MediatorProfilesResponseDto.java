package com.coddicted.buzzma.mediator.api;

import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class MediatorProfilesResponseDto {

  UUID id;

  UUID userId;

  String mediatorCode;

  @Nullable String parentAgencyCode;

  String status;

  Boolean isDeleted;

  @Nullable Instant createdAt;

  @Nullable Instant updatedAt;
}
