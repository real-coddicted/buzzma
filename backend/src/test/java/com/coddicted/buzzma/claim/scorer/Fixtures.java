package com.coddicted.buzzma.claim.scorer;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.extraction.entity.ScoredValue;
import com.coddicted.buzzma.shared.score.PayloadItem;
import com.coddicted.buzzma.shared.util.FileUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class Fixtures {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  static final UUID JOB_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  static final UUID SCREENSHOT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

  static final Campaign CAMPAIGN =
      FileUtils.loadResourceAsObject("/fixtures/input/claim/scorer/campaign.json", Campaign.class);

  static final Claim CLAIM =
      FileUtils.loadResourceAsObject("/fixtures/input/claim/scorer/claim.json", Claim.class);

  static Map<String, ScoredValue> loadExtractedDetails(final String resourcePath) {
    return readValue(resourcePath, new TypeReference<Map<String, ScoredValue>>() {});
  }

  static List<PayloadItem> loadPayload(final String resourcePath) {
    return readValue(resourcePath, new TypeReference<List<PayloadItem>>() {});
  }

  private static <T> T readValue(final String resourcePath, final TypeReference<T> typeReference) {
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
}
