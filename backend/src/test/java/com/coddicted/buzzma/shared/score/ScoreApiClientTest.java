package com.coddicted.buzzma.shared.score;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

@SpringBootTest(
    classes = {ScoreApiClientTest.TestConfig.class, ScoreApiClient.class, ScoreApiProperties.class})
@TestPropertySource(
    properties = {
      "app.score.retry.max-attempts=3",
      "app.score.retry.initial-delay-ms=1",
      "app.score.retry.multiplier=1"
    })
class ScoreApiClientTest {

  private static final String SCORE_URL = "http://localhost:8082/api/v1/score";

  @Autowired private ScoreApiClient scoreApiClient;
  @Autowired private MockRestServiceServer mockServer;

  @Test
  void retriesOnServerErrorThenSucceeds() {
    mockServer.expect(requestTo(SCORE_URL)).andRespond(withServerError());
    mockServer.expect(requestTo(SCORE_URL)).andRespond(withServerError());
    mockServer
        .expect(requestTo(SCORE_URL))
        .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

    final List<ScoreResponseDto> result = scoreApiClient.score(List.of());

    assertThat(result).isEmpty();
    mockServer.verify();
  }

  @Test
  void doesNotRetryOnClientError() {
    mockServer.expect(requestTo(SCORE_URL)).andRespond(withStatus(HttpStatus.BAD_REQUEST));

    assertThatThrownBy(() -> scoreApiClient.score(List.of())).isInstanceOf(ScoreApiException.class);

    mockServer.verify();
  }

  @Test
  void wrapsExceptionAfterExhaustingRetriesOnServerError() {
    mockServer.expect(requestTo(SCORE_URL)).andRespond(withServerError());
    mockServer.expect(requestTo(SCORE_URL)).andRespond(withServerError());
    mockServer.expect(requestTo(SCORE_URL)).andRespond(withServerError());

    assertThatThrownBy(() -> scoreApiClient.score(List.of())).isInstanceOf(ScoreApiException.class);

    mockServer.verify();
  }

  @EnableRetry
  @Configuration
  static class TestConfig {

    private final RestClient.Builder builder = RestClient.builder();

    @Bean
    RestClient.Builder restClientBuilder(final MockRestServiceServer mockServer) {
      return builder;
    }

    @Bean
    MockRestServiceServer mockServer() {
      return MockRestServiceServer.bindTo(builder).build();
    }
  }
}
