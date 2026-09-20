package com.coddicted.buzzma.claim.client;

import java.util.Map;

public interface GeminiClientProxy {
  /**
   * Runs {@code prompt} against the image and returns the extracted fields as a flat string map.
   */
  Map<String, String> extract(final String prompt, final byte[] imageBytes, final String mimeType);
}
