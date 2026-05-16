package com.coddicted.buzzma.order.entity;

import java.util.Map;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class Screenshot {
  String key;
  ScreenshotType type;
  boolean verified;
  double score;
  Map<String, String> extractedDetails;
}
