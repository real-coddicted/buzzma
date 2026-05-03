package com.coddicted.buzzma.identity.dto;

import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class SecurityQuestionResponseDto {

  UUID id;

  UUID userId;

  UUID questionId;

  @Nullable Instant createdAt;

  @Nullable Instant updatedAt;
}
