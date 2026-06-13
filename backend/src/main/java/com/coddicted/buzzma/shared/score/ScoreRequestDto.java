package com.coddicted.buzzma.shared.score;

import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ScoreRequestDto {
  String key;
  List<PayloadItem> payload;
}
