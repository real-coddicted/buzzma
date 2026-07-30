package com.coddicted.buzzma.claim.dto;

import com.coddicted.buzzma.claim.entity.ReviewerDecision;
import jakarta.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.UUID;
import lombok.Data;

@Data
public class ClaimReviewRequestDto {

  private UUID claimId;

  @NotNull private ReviewerDecision reviewerDecision;

  private String reviewerComment;

  private BigInteger amountApprovedPaise;
}
