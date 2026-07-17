package com.coddicted.buzzma.shared.score;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class ScoreApiClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(ScoreApiClient.class);

  private final RestClient restClient;

  public ScoreApiClient(final RestClient.Builder builder, final ScoreApiProperties properties) {
    this.restClient = builder.baseUrl(properties.getBaseUrl()).build();
    LOGGER.info("ScoreApiClient initialized: baseUrl={}", properties.getBaseUrl());
  }

  public List<ScoreResponseDto> score(
      final List<ScoreRequestDto> requests, final ScoringAlgorithm algorithm) {
    LOGGER.debug(
        "score: sending {} request(s) to /api/v1/score with algorithm={}",
        requests.size(),
        algorithm);
    try {
      return restClient
          .post()
          .uri(
              uriBuilder ->
                  uriBuilder
                      .path("/api/v1/score")
                      .queryParam("algorithm", algorithm.getValue())
                      .build())
          .contentType(MediaType.APPLICATION_JSON)
          .body(requests)
          .retrieve()
          .body(new ParameterizedTypeReference<>() {});
    } catch (final RestClientException e) {
      LOGGER.warn("Score API call failed: {}", e.getMessage());
      throw new ScoreApiException("Score API call failed: " + e.getMessage(), e);
    }
  }
}
