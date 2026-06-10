package com.coddicted.buzzma.extraction.entity;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ReviewExtractionResult {
  String productName;
  String reviewText;
  String accountName;
  String reviewDate;
}
