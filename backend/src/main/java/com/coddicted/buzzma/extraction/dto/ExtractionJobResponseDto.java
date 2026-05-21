package com.coddicted.buzzma.extraction.dto;

import com.coddicted.buzzma.extraction.entity.ExtractionJobStatus;
import com.coddicted.buzzma.extraction.entity.ExtractionResult;
import com.coddicted.buzzma.extraction.entity.ValidationError;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ExtractionJobResponseDto {
  UUID id;
  UUID submittedBy;
  ExtractionJobStatus status;
  String originalFilename;
  int attemptCount;
  String errorMessage;
  ExtractionResult result;
  List<ValidationError> validationErrors;
  Instant createdAt;
  Instant updatedAt;
}
