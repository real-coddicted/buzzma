package com.coddicted.buzzma.order.dto;

import com.coddicted.buzzma.campaign.dto.DealResponseDto;
import com.coddicted.buzzma.order.entity.Screenshot;
import com.coddicted.buzzma.shared.enums.OrderWorkflowStatus;
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
public class OrderResponseDto {
  UUID id;
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
  List<Screenshot> screenshots;
  Boolean overallVerified;
  Double overallScore;
  String rejectionNote;
  String comments;
  Instant createdAt;
  Instant updatedAt;
}
