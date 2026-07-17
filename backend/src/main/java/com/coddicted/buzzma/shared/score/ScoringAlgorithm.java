package com.coddicted.buzzma.shared.score;

public enum ScoringAlgorithm {
  WEIGHTED_AVERAGE("weighted_average"),
  PENALIZED_WEIGHTED_AVERAGE("penalized_weighted_average"),
  MIN_VALUE("min_value");

  private final String value;

  ScoringAlgorithm(final String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
