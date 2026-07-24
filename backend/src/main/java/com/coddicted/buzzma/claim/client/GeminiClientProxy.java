package com.coddicted.buzzma.claim.client;

import com.coddicted.buzzma.claim.entity.ScreenshotType;

public interface GeminiClientProxy {
  <T> T extract(
      final ScreenshotType screenshotType,
      final byte[] imageBytes,
      final String mimeType,
      final Class<T> valueType);
}
