package com.coddicted.buzzma.shared.score;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class PayloadItem {
  String label;
  String expected;
  String actual;
}
