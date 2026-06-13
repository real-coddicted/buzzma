package com.coddicted.buzzma.shared.score;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ScoreResponseDto {
  String key;

  @JsonProperty("overall_score")
  double overallScore;

  List<LabelScore> scores;
}
