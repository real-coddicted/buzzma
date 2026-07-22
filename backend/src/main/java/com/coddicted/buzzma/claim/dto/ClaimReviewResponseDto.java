package com.coddicted.buzzma.claim.dto;

import com.coddicted.buzzma.claim.entity.ClaimReviewStatus;
import com.coddicted.buzzma.shared.enums.Platform;
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
  String campaignCode;

  UUID dealId;
  UUID dealOwnerId;
  String dealOwnerName;
  String dealOwnerCode;

  String buyerName;
  String buyerCode;

  UUID claimId;
  String claimStatus;
  String ecommerceOrderId;

  Boolean mediatorVerified;
  BigInteger matchScore;

  ClaimReviewStatus claimReviewStatus;

  Platform platform;
  int orderDate;
  String brandName;

  Instant createdAt;
  Instant updatedAt;
}
