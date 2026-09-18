package com.coddicted.buzzma.claim.dto;

import com.coddicted.buzzma.claim.entity.WorksheetRowStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimReviewWorksheetRowResponseDto {

  private UUID id;
  private UUID worksheetId;
  private String campaign;
  private String campaignCode;
  private String brand;
  private String mediator;
  private String buyer;
  private String profileName;
  private String platform;
  private String orderId;
  private String orderDate;
  private String orderAmount;
  private String claimCode;
  private String claimStatus;
  private String matchScore;
  private String amountApproved;
  private String brandReview;
  private String remarks;
  private WorksheetRowStatus processingStatus;
  private String errorRemarks;
  private int retryCount;
  private Instant lastAttemptedAt;
}
