package com.coddicted.buzzma.claim.processor;

import com.coddicted.buzzma.claim.client.GeminiClientProxy;
import com.coddicted.buzzma.claim.entity.ScreenshotType;
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

  static Map<String, ScoredValue> loadExtractedDetails(final String resourcePath) {
    try (InputStream stream = Fixtures.class.getResourceAsStream(resourcePath)) {
      if (stream == null) {
        throw new IllegalArgumentException("file resource not found: " + resourcePath);
      }
      return MAPPER.readValue(stream, new TypeReference<Map<String, ScoredValue>>() {});
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private Fixtures() {}

  /**
   * Fake GeminiClientProxy returning a fixed, fixture-loaded extraction result regardless of the
   * image bytes given, while recording the arguments it was called with for assertions. Used
   * instead of a Mockito mock because the real byte[] argument (round-tripped through
   * StorageService/ResponseBytes) has no reliable value-based equality to stub against.
   */
  static final class FixedResultGeminiClientProxy implements GeminiClientProxy {
    private final Object result;
    ScreenshotType lastScreenshotType;
    byte[] lastImageBytes;
    String lastMimeType;
    Class<?> lastValueType;

    FixedResultGeminiClientProxy(final Object result) {
      this.result = result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T extract(
        final ScreenshotType screenshotType,
        final byte[] imageBytes,
        final String mimeType,
        final Class<T> valueType) {
      this.lastScreenshotType = screenshotType;
      this.lastImageBytes = imageBytes;
      this.lastMimeType = mimeType;
      this.lastValueType = valueType;
      return (T) this.result;
    }
  }
}
