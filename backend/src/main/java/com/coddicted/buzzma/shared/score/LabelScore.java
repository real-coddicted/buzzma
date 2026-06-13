package com.coddicted.buzzma.shared.score;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class LabelScore {
  String label;

  @JsonProperty("score")
  double score;
}
