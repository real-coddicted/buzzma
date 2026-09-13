package com.coddicted.buzzma.claim.processor;

import com.coddicted.buzzma.claim.client.GeminiClientProxy;
import com.coddicted.buzzma.extraction.entity.ScoredValue;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.UUID;

final class Fixtures {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  static final UUID JOB_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  static final UUID SCREENSHOT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  static final UUID CLAIM_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
  static final String STORAGE_KEY = "claims/screenshot.jpg";
  static final String MIME_TYPE = "image/jpeg";
  static final byte[] IMAGE_BYTES = {1, 2, 3};

  static Map<String, String> loadExtractionResult(final String resourcePath) {
    return load(resourcePath, new TypeReference<Map<String, String>>() {});
  }

  static Map<String, ScoredValue> loadExtractedDetails(final String resourcePath) {
    return load(resourcePath, new TypeReference<Map<String, ScoredValue>>() {});
  }

  private static <T> T load(final String resourcePath, final TypeReference<T> typeReference) {
    try (InputStream stream = Fixtures.class.getResourceAsStream(resourcePath)) {
      if (stream == null) {
        throw new IllegalArgumentException("file resource not found: " + resourcePath);
      }
      return MAPPER.readValue(stream, typeReference);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private Fixtures() {}

  /**
   * Fake GeminiClientProxy returning a fixed, fixture-loaded field map regardless of the prompt or
   * image bytes given, while recording the arguments it was called with for assertions. Used
   * instead of a Mockito mock because the real byte[] argument (round-tripped through
   * StorageService/ResponseBytes) has no reliable value-based equality to stub against.
   */
  static final class FixedResultGeminiClientProxy implements GeminiClientProxy {
    private final Map<String, String> result;
    String lastPrompt;
    byte[] lastImageBytes;
    String lastMimeType;

    FixedResultGeminiClientProxy(final Map<String, String> result) {
      this.result = result;
    }

    @Override
    public Map<String, String> extract(
        final String prompt, final byte[] imageBytes, final String mimeType) {
      this.lastPrompt = prompt;
      this.lastImageBytes = imageBytes;
      this.lastMimeType = mimeType;
      return this.result;
    }
  }
}
