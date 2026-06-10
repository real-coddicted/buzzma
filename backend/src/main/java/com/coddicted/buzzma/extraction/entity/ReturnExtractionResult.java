package com.coddicted.buzzma.extraction.entity;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ReturnExtractionResult {
  String productName;
  String accountName;
  String returnWindowClosedText;
  String returnWindowClosedDate;
}
