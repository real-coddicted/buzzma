package com.coddicted.buzzma.extraction.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoredValue {
  String extractedValue;
  Integer score;
  boolean isMismatch;
}
