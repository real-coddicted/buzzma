package com.coddicted.buzzma.claim.dto;

import com.coddicted.buzzma.claim.entity.ClaimReviewStatus;
import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ClaimReviewResponseDto {

  UUID id;
  UUID campaignId;
  String campaignName;

  UUID dealId;
  UUID dealOwnerId;
  String dealOwnerName;

  UUID claimId;
  String claimStatus;
  String ecommerceOrderId;

  Boolean mediatorVerified;
  BigInteger matchScore;

  ClaimReviewStatus claimReviewStatus;

  Instant createdAt;
  Instant updatedAt;
}
