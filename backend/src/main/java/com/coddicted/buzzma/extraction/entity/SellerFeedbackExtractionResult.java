package com.coddicted.buzzma.extraction.entity;

import com.coddicted.buzzma.shared.enums.Platform;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class SellerFeedbackExtractionResult {
  Platform platform;
  String sellerName;
  String productName;
  String orderId;
  Integer rating;
  String feedbackText;
  String comment;
}
