package com.coddicted.buzzma.shared.communications;

import com.coddicted.buzzma.shared.security.JwtService;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class CommunicationsClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(CommunicationsClient.class);

  private final RestClient restClient;
  private final JwtService jwtService;

  public CommunicationsClient(
      final RestClient.Builder builder,
      final CommunicationsProperties properties,
      final JwtService jwtService) {
    this.restClient = builder.baseUrl(properties.getBaseUrl()).build();
    this.jwtService = jwtService;
    LOGGER.info("CommunicationsClient initialized: baseUrl={}", properties.getBaseUrl());
  }

  @Retryable(
      retryFor = RestClientException.class,
      maxAttemptsExpression = "${app.communications.max-attempts:3}",
      backoff =
          @Backoff(
              delayExpression = "${app.communications.retry-backoff-ms:200}",
              multiplierExpression = "${app.communications.retry-backoff-multiplier:2.0}"))
  public UUID sendEmail(
      final String to, final String subject, final String body, final UUID requestedBy) {
    try {
      final SendEmailResponseDto response =
          this.restClient
              .post()
              .uri("/api/email/messages")
              .header(
                  HttpHeaders.AUTHORIZATION,
                  "Bearer " + this.jwtService.generateAccessToken(requestedBy))
              .contentType(MediaType.APPLICATION_JSON)
              .body(
                  SendEmailRequestDto.builder()
                      .to(to)
                      .subject(subject)
                      .body(body)
                      .requestedBy(requestedBy.toString())
                      .build())
              .retrieve()
              .body(SendEmailResponseDto.class);
      return response.getRequestId();
    } catch (final RestClientException e) {
      LOGGER.warn("Communications API call failed: {}", e.getMessage());
      throw e;
    }
  }

  @Recover
  public UUID recover(
      final RestClientException e,
      final String to,
      final String subject,
      final String body,
      final UUID requestedBy) {
    throw new CommunicationsApiException(
        "Communications API call failed after retries: " + e.getMessage(), e);
  }
}
