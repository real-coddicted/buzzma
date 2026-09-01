package com.coddicted.buzzma.claim.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ClaimScreenshotProcessorUtilsTest {

  @Test
  void normalizeOrderIdStripsLeadingHashAndAllWhitespace() {
    assertEquals(
        "134005059763414304501",
        ClaimScreenshotProcessorUtils.normalizeOrderId("# 1340050 59763414304501"));
  }

  @Test
  void normalizeOrderIdKeepsInternalHyphens() {
    assertEquals(
        "403-1234567-8901234",
        ClaimScreenshotProcessorUtils.normalizeOrderId("  403-1234567-8901234 "));
  }

  @Test
  void normalizeOrderIdLeavesCleanValueUnchanged() {
    assertEquals(
        "OD123456789012", ClaimScreenshotProcessorUtils.normalizeOrderId("OD123456789012"));
  }

  @Test
  void normalizeOrderIdReturnsNullForNullOrBlank() {
    assertNull(ClaimScreenshotProcessorUtils.normalizeOrderId(null));
    assertNull(ClaimScreenshotProcessorUtils.normalizeOrderId("   "));
    assertNull(ClaimScreenshotProcessorUtils.normalizeOrderId("#  "));
  }
}
