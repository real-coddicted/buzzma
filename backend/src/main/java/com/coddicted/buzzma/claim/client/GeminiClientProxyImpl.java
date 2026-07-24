package com.coddicted.buzzma.claim.client;

import com.coddicted.buzzma.claim.entity.ScreenshotType;
import com.coddicted.buzzma.claim.utils.ClaimScreenshotProcessorUtils;
import com.coddicted.buzzma.extraction.service.GeminiExtractionPromptBuilder;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.gemini.GeminiClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GeminiClientProxyImpl implements GeminiClientProxy {

  private static final Logger LOGGER = LoggerFactory.getLogger(GeminiClientProxyImpl.class);
  private final GeminiClient geminiClient;
  private final GeminiExtractionPromptBuilder promptBuilder;
  private final ObjectMapper objectMapper;

  public GeminiClientProxyImpl(
      final GeminiClient geminiClient,
      final GeminiExtractionPromptBuilder promptBuilder,
      final ObjectMapper objectMapper) {
    this.geminiClient = geminiClient;
    this.promptBuilder = promptBuilder;
    this.objectMapper = objectMapper;
  }

  @Override
  public <T> T extract(
      final ScreenshotType screenshotType,
      final byte[] imageBytes,
      final String mimeType,
      final Class<T> valueType) {
    final String rawText =
        this.geminiClient.generateContent(getPrompt(screenshotType), imageBytes, mimeType);
    final String json = ClaimScreenshotProcessorUtils.sanitizeJson(rawText);
    try {
      return this.objectMapper.readValue(json, valueType);
    } catch (final Exception e) {
      LOGGER.warn("Extraction failed: {} {}", json, e.getMessage());
      throw new BusinessRuleViolationException("Extraction failed: " + e.getMessage());
    }
  }

  private String getPrompt(final ScreenshotType screenshotType) {
    return switch (screenshotType) {
      case ScreenshotType.SCREENSHOT_TYPE_ORDER -> this.promptBuilder.build();
      case ScreenshotType.SCREENSHOT_TYPE_RATING -> this.promptBuilder.buildRatingPrompt();
      case ScreenshotType.SCREENSHOT_TYPE_REVIEW -> this.promptBuilder.buildReviewPrompt();
      case ScreenshotType.SCREENSHOT_TYPE_RETURN -> this.promptBuilder.buildReturnPrompt();
    };
  }
}
