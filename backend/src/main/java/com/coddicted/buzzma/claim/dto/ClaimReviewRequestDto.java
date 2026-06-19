package com.coddicted.buzzma.claim.dto;

import com.coddicted.buzzma.claim.entity.ReviewerDecision;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClaimReviewRequestDto {

  @NotNull private ReviewerDecision reviewerDecision;

  private String reviewerComment;
}
