package com.coddicted.buzzma.shared.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public final class FileUtils {

  private static final ObjectMapper MAPPER =
      new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  private FileUtils() {}

  public static <T> T loadResourceAsObject(final String resourcePath, final Class<T> type) {
    try (InputStream stream = FileUtils.class.getResourceAsStream(resourcePath)) {
      if (stream == null) {
        throw new IllegalArgumentException("file resource not found: " + resourcePath);
      }
      return MAPPER.readValue(stream, type);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static String loadResourceAsString(final String resourcePath) {
    try (InputStream stream = FileUtils.class.getResourceAsStream(resourcePath)) {
      if (stream == null) {
        throw new IllegalArgumentException("file resource not found: " + resourcePath);
      }
      return new String(stream.readAllBytes());
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
