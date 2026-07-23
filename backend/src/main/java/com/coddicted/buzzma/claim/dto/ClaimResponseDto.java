package com.coddicted.buzzma.claim.dto;

import com.coddicted.buzzma.campaign.dto.DealResponseDto;
import com.coddicted.buzzma.claim.entity.ClaimReviewStatus;
import com.coddicted.buzzma.claim.entity.ClaimStatus;
import com.coddicted.buzzma.shared.enums.Platform;
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
  String code;
  DealResponseDto deal;
  ClaimStatus status;
  int currentStep;
  String ecommerceOrderId;
  BigInteger amountPaise;
  String productName;
  String sellerName;
  int orderDate;
  String accountName;
  String orderedBy;
  String reviewUrl;
  List<ClaimScreenshotResponseDto> screenshots;
  Boolean mediatorVerified;
  Integer score;
  String reviewerComments;
  UUID reviewerId;
  ClaimReviewStatus reviewStatus;
  Platform platform;
  Instant createdAt;
  Instant updatedAt;
}
