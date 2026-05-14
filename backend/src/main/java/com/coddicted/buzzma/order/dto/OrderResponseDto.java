package com.coddicted.buzzma.order.dto;

import com.coddicted.buzzma.shared.enums.OrderWorkflowStatus;
import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class OrderResponseDto {
  UUID id;
  UUID campaignId;
  DealResponseDto deal;
  OrderWorkflowStatus status;
  int currentStep;
  String ecommerceOrderId;
  BigInteger amountPaise;
  String productName;
  String sellerName;
  String orderDate;
  String accountName;
  String reviewUrl;
  boolean hasOrderScreenshot;
  boolean hasReviewScreenshot;
  boolean hasReturnScreenshot;
  String rejectionNote;
  Instant createdAt;
  Instant updatedAt;
}
