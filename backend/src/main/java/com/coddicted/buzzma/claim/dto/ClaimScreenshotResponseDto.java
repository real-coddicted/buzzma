package com.coddicted.buzzma.claim.dto;

import com.coddicted.buzzma.claim.entity.ScreenshotType;
import com.coddicted.buzzma.claim.entity.ScreenshotVerificationStatus;
import com.coddicted.buzzma.extraction.entity.ScoredValue;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ClaimScreenshotResponseDto {
  UUID id;
  String storageKey;
  ScreenshotType type;
  ScreenshotVerificationStatus verificationStatus;
  Integer score;
  Map<String, ScoredValue> extractedDetails;
  Instant createdAt;
}
