package com.coddicted.buzzma.feedback.dto;

import com.coddicted.buzzma.feedback.entity.FeedbackCategory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class FeedbackRequestDto {

  @Min(1)
  @Max(5)
  int rating;

  @NotNull FeedbackCategory category;

  @NotBlank String feedback;
}
