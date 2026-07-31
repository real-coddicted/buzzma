package com.coddicted.buzzma.shared.gemini;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.google.genai.types.ThinkingConfig;
import com.google.genai.types.ThinkingLevel;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GeminiClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(GeminiClient.class);

  private final GeminiProperties properties;
  private final Client client;

  public GeminiClient(final GeminiProperties properties) {
    this.properties = properties;
    this.client = Client.builder().apiKey(properties.getApiKey()).build();
    final String apiKey = properties.getApiKey();
    LOGGER.info(
        "GeminiClient initialized: model={}, apiKeyPresent={}",
        properties.getModel(),
        apiKey != null && !apiKey.isBlank());
  }

  public String generateContent(
      final String prompt, final byte[] imageBytes, final String mimeType) {
    final GenerateContentConfig config =
        GenerateContentConfig.builder()
            .thinkingConfig(
                ThinkingConfig.builder().thinkingLevel(ThinkingLevel.Known.MINIMAL).build())
            .build();

    LOGGER.info("Gemini model in use: {}", properties.getModel());

    try {
      final GenerateContentResponse response =
          client.models.generateContent(
              properties.getModel(),
              List.of(
                  Content.fromParts(Part.fromText(prompt), Part.fromBytes(imageBytes, mimeType))),
              config);
      // TODO this logger should be removed/ changed
      LOGGER.info("Gemini model response: {}", response);
      return response.text();
    } catch (Exception e) {
      LOGGER.error("Gemini API call failed: ", e);
      throw new GeminiException("Gemini API call failed: " + e.getMessage(), e);
    }
  }
}
