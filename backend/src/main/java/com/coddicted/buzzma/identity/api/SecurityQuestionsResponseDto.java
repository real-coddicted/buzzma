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
public class SecurityQuestionsResponseDto {

  UUID id;

  UUID userId;

  UUID questionId;

  @Nullable Instant createdAt;

  @Nullable Instant updatedAt;
}
