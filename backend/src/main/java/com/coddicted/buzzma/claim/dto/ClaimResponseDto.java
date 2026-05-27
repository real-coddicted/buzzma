package com.coddicted.buzzma.claim.dto;

import com.coddicted.buzzma.campaign.dto.DealResponseDto;
import com.coddicted.buzzma.claim.entity.ClaimReviewStatus;
import com.coddicted.buzzma.claim.entity.ClaimStatus;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ClaimResponseDto {
  UUID id;
  DealResponseDto deal;
  ClaimStatus status;
  int currentStep;
  String ecommerceOrderId;
  BigInteger amountPaise;
  String productName;
  String sellerName;
  int orderDate;
  String accountName;
  String reviewUrl;
  List<ClaimScreenshotResponseDto> screenshots;
  Boolean mediatorVerified;
  BigInteger score;
  String reviewerComments;
  UUID reviewerId;
  ClaimReviewStatus reviewStatus;
  Instant createdAt;
  Instant updatedAt;
}
