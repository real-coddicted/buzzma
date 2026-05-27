package com.coddicted.buzzma.extraction.entity;

import com.coddicted.buzzma.shared.enums.Platform;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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
  List<ValidationError> validationErrors = new ArrayList<>();
}
