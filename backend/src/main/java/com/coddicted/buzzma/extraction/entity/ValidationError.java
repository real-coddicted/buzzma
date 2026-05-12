package com.coddicted.buzzma.extraction.entity;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ValidationError {
  String field;
  String message;
}
