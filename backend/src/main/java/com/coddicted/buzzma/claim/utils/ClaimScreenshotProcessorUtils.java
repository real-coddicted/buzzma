package com.coddicted.buzzma.claim.utils;

public final class ClaimScreenshotProcessorUtils {
  private ClaimScreenshotProcessorUtils() {}

  public static String sanitizeJson(final String raw) {
    String trimmed = raw.strip();
    if (trimmed.startsWith("```")) {
      trimmed = trimmed.replaceFirst("```[a-z]*\\n?", "");
      final int lastFence = trimmed.lastIndexOf("```");
      if (lastFence >= 0) {
        trimmed = trimmed.substring(0, lastFence);
      }
      trimmed = trimmed.strip();
    }
    return trimmed;
  }

  /**
   * Normalizes an extracted order identifier. Gemini often returns the value with the on-screen
   * label decoration (a leading {@code #}) and with the spaces some platforms render inside the
   * number. This removes all whitespace and any leading {@code #} characters. Internal hyphens are
   * preserved, since they are significant for platforms such as Amazon and Nykaa.
   *
   * @return the cleaned identifier, or {@code null} if the input is null or blank
   */
  public static String normalizeOrderId(final String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    final String cleaned = raw.replaceAll("\\s", "").replaceFirst("^#+", "");
    return cleaned.isBlank() ? null : cleaned;
  }

  public static String mimeTypeFromFilename(final String filename) {
    if (filename == null) {
      return "image/jpeg";
    }
    final String lower = filename.toLowerCase();
    if (lower.endsWith(".png")) {
      return "image/png";
    }
    if (lower.endsWith(".gif")) {
      return "image/gif";
    }
    if (lower.endsWith(".webp")) {
      return "image/webp";
    }
    return "image/jpeg";
  }
}
