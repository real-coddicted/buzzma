package com.coddicted.buzzma.extraction.entity;

import com.coddicted.buzzma.shared.enums.Platform;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class DeliveryExtractionResult {
  Platform platform;
  String productName;
  String orderId;
  String deliveryDate;
  String deliveryStatus;
}
