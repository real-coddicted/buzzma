package com.coddicted.buzzma.shared.gemini;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class GeminiClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(GeminiClient.class);

  private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models";

  private final GeminiProperties properties;
  private final RestClient restClient;

  public GeminiClient(final RestClient.Builder builder, final GeminiProperties properties) {
    this.properties = properties;
    this.restClient = builder.baseUrl(BASE_URL).build();
    final String apiKey = properties.getApiKey();
    LOGGER.info(
        "GeminiClient initialized: model={}, apiKeyPresent={}",
        properties.getModel(),
        apiKey != null && !apiKey.isBlank());
  }

  public String generateContent(
      final String prompt, final byte[] imageBytes, final String mimeType) {
    final String base64 = Base64.getEncoder().encodeToString(imageBytes);

    final Map<String, Object> inlineData = Map.of("mimeType", mimeType, "data", base64);
    final Map<String, Object> imagePart = Map.of("inlineData", inlineData);
    final Map<String, Object> textPart = Map.of("text", prompt);
    final Map<String, Object> content = Map.of("parts", List.of(textPart, imagePart));
    final Map<String, Object> body = Map.of("contents", List.of(content));

    LOGGER.info("Gemini model in use: {}", properties.getModel());

    try {
      final Map<?, ?> response =
          restClient
              .post()
              .uri(
                  "/{model}:generateContent?key={key}",
                  properties.getModel(),
                  properties.getApiKey())
              .contentType(MediaType.APPLICATION_JSON)
              .body(body)
              .retrieve()
              .body(Map.class);
      // TODO this logger should be removed/ changed
      LOGGER.info("Gemini model response: {}", response);
      return extractText(response);
    } catch (RestClientException e) {
      LOGGER.warn("Gemini API call failed: {}", e.getMessage());
      throw new GeminiException("Gemini API call failed: " + e.getMessage(), e);
    }
  }

  @SuppressWarnings("unchecked")
  private String extractText(final Map<?, ?> response) {
    try {
      final List<?> candidates = (List<?>) response.get("candidates");
      final Map<?, ?> first = (Map<?, ?>) candidates.get(0);
      final Map<?, ?> content = (Map<?, ?>) first.get("content");
      final List<?> parts = (List<?>) content.get("parts");
      final Map<?, ?> part = (Map<?, ?>) parts.get(0);
      return (String) part.get("text");
    } catch (Exception e) {
      throw new GeminiException("Unexpected Gemini response structure", e);
    }
  }
}
