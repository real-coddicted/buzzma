package com.coddicted.buzzma.extraction.entity;

import com.coddicted.buzzma.shared.enums.Platform;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ExtractionResult {
  Platform platform;
  String orderId;
  String orderDate;
  String productName;
  String sellerName;
  BigDecimal amount;
  String orderedBy;
  List<ValidationError> validationErrors;
  Map<String, ScoredValue> extractedResult;
  Double overallScore;
}
