package com.coddicted.buzzma.claim.client;

import com.coddicted.buzzma.claim.utils.ClaimScreenshotProcessorUtils;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.gemini.GeminiClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GeminiClientProxyImpl implements GeminiClientProxy {

  private static final Logger LOGGER = LoggerFactory.getLogger(GeminiClientProxyImpl.class);
  private final GeminiClient geminiClient;
  private final ObjectMapper objectMapper;

  public GeminiClientProxyImpl(final GeminiClient geminiClient, final ObjectMapper objectMapper) {
    this.geminiClient = geminiClient;
    this.objectMapper = objectMapper;
  }

  @Override
  public Map<String, String> extract(
      final String prompt, final byte[] imageBytes, final String mimeType) {
    final String rawText = this.geminiClient.generateContent(prompt, imageBytes, mimeType);
    final String json = ClaimScreenshotProcessorUtils.sanitizeJson(rawText);
    try {
      final Map<String, Object> raw =
          this.objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
      final Map<String, String> result = new HashMap<>();
      raw.forEach((key, value) -> result.put(key, value != null ? value.toString() : null));
      return result;
    } catch (final Exception e) {
      LOGGER.warn("Extraction failed: {} {}", json, e.getMessage());
      throw new BusinessRuleViolationException("Extraction failed: " + e.getMessage());
    }
  }
}
