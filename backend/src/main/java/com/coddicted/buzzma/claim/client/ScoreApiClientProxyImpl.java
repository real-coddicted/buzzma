package com.coddicted.buzzma.claim.client;

import com.coddicted.buzzma.extraction.entity.ScoredValue;
import com.coddicted.buzzma.shared.score.PayloadItem;
import com.coddicted.buzzma.shared.score.ScoreApiClient;
import com.coddicted.buzzma.shared.score.ScoreRequestDto;
import com.coddicted.buzzma.shared.score.ScoreResponseDto;
import com.coddicted.buzzma.shared.score.ScoringAlgorithm;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ScoreApiClientProxyImpl implements ScoreApiClientProxy {

  private final ScoreApiClient scoreApiClient;

  public ScoreApiClientProxyImpl(final ScoreApiClient scoreApiClient) {
    this.scoreApiClient = scoreApiClient;
  }

  /**
   * Sends a batch of labeled expected/actual values to the Score API under the given key and maps
   * the response back into {@link ScoredValue}s keyed by label.
   */
  @Override
  public ExtractedScoredResult score(final String key, final List<PayloadItem> payload) {
    final ScoreRequestDto request = ScoreRequestDto.builder().key(key).payload(payload).build();
    final List<ScoreResponseDto> scoreResponses =
        this.scoreApiClient.score(List.of(request), ScoringAlgorithm.MIN_VALUE);
    final ScoreResponseDto response =
        scoreResponses.stream()
            .filter(r -> key.equals(r.getKey()))
            .findFirst()
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Score API returned no result for key '" + key + "'"));

    final Map<String, Double> labelScores =
        response.getScores().stream()
            .collect(Collectors.toMap(ls -> ls.getLabel(), ls -> ls.getScore()));

    final Map<String, ScoredValue> map = new HashMap<>();
    for (final PayloadItem item : payload) {
      final Double rawScore = labelScores.get(item.getLabel());
      map.put(
          item.getLabel(),
          ScoredValue.builder()
              .extractedValue(item.getActual())
              .score(rawScore != null ? (int) Math.round(rawScore * 100) : null)
              .build());
    }
    return new ExtractedScoredResult(map, (int) Math.round(response.getOverallScore() * 100));
  }
}
