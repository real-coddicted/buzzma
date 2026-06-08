package com.coddicted.buzzma.extraction.dto;

import com.coddicted.buzzma.extraction.entity.ExtractionJobStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ExtractionJobResponseDto {
  UUID id;
  UUID claimScreenshotId;
  ExtractionJobStatus status;
  int attemptCount;
  String errorMessage;
  Instant createdAt;
  Instant updatedAt;
}
