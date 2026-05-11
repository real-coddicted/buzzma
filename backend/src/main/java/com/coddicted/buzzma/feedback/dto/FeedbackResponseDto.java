package com.coddicted.buzzma.feedback.dto;

import com.coddicted.buzzma.feedback.entity.FeedbackCategory;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class FeedbackResponseDto {

  UUID id;
  UUID userId;
  int rating;
  FeedbackCategory category;
  String feedback;
  Instant createdAt;
}
