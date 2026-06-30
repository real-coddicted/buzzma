package com.coddicted.buzzma.shared.score;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
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

  @Retryable(
      retryFor = {HttpServerErrorException.class, ResourceAccessException.class},
      maxAttemptsExpression = "${app.score.retry.max-attempts:3}",
      backoff =
          @Backoff(
              delayExpression = "${app.score.retry.initial-delay-ms:500}",
              multiplierExpression = "${app.score.retry.multiplier:2.0}"))
  public List<ScoreResponseDto> score(final List<ScoreRequestDto> requests) {
    LOGGER.debug("score: sending {} request(s) to /api/v1/score", requests.size());
    return restClient
        .post()
        .uri("/api/v1/score")
        .contentType(MediaType.APPLICATION_JSON)
        .body(requests)
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});
  }

  @Recover
  List<ScoreResponseDto> recover(
      final RestClientException e, final List<ScoreRequestDto> requests) {
    LOGGER.warn("Score API call failed: {}", e.getMessage());
    throw new ScoreApiException("Score API call failed: " + e.getMessage(), e);
  }
}
