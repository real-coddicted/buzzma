package com.coddicted.buzzma.claim.utils;

import static org.junit.jupiter.api.Assertions.*;

import com.coddicted.buzzma.claim.client.ExtractedScoredResult;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.extraction.entity.ScoredValue;
import com.coddicted.buzzma.shared.constants.BuzzmahConstants;
import com.coddicted.buzzma.shared.enums.Platform;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ClaimScreenshotScorerUtilsTest {

  private Claim buildClaim(
      Platform platform,
      String ecommerceOrderId,
      int orderDate,
      BigInteger amountPaise,
      String accountName) {
    return Claim.builder()
        .platform(platform)
        .ecommerceOrderId(ecommerceOrderId)
        .orderDate(orderDate)
        .amountPaise(amountPaise)
        .accountName(accountName)
        .build();
  }

  private Map<String, ScoredValue> details(
      String platform, String orderId, String orderDate, String amount, String orderedBy) {
    Map<String, ScoredValue> map = new HashMap<>();
    if (platform != null)
      map.put(BuzzmahConstants.PLATFORM, ScoredValue.builder().extractedValue(platform).build());
    if (orderId != null)
      map.put(BuzzmahConstants.ORDER_ID, ScoredValue.builder().extractedValue(orderId).build());
    if (orderDate != null)
      map.put(BuzzmahConstants.ORDER_DATE, ScoredValue.builder().extractedValue(orderDate).build());
    if (amount != null)
      map.put(BuzzmahConstants.AMOUNT, ScoredValue.builder().extractedValue(amount).build());
    if (orderedBy != null)
      map.put(BuzzmahConstants.ORDERED_BY, ScoredValue.builder().extractedValue(orderedBy).build());
    return map;
  }

  @Test
  void allFieldsMatchNoMismatchAndScoreUnchanged() {
    Claim claim =
        buildClaim(
            Platform.PLATFORM_AMAZON, "ORD-123", 20260615, BigInteger.valueOf(50000), "john.doe");
    Map<String, ScoredValue> input =
        details("PLATFORM_AMAZON", "ORD-123", "2026-06-15", "500.00", "john.doe");

    ExtractedScoredResult result =
        ClaimScreenshotScorerUtils.updateExtractedDataForMatchWithManualEntryInOrder(
            claim, input, 90);

    assertFalse(result.extractedResult().get(BuzzmahConstants.PLATFORM).isMismatch());
    assertFalse(result.extractedResult().get(BuzzmahConstants.ORDER_ID).isMismatch());
    assertFalse(result.extractedResult().get(BuzzmahConstants.ORDER_DATE).isMismatch());
    assertFalse(result.extractedResult().get(BuzzmahConstants.AMOUNT).isMismatch());
    assertFalse(result.extractedResult().get(BuzzmahConstants.ORDERED_BY).isMismatch());
    assertEquals(90, result.overallScore());
  }

  @Test
  void orderIdMismatchForcesScoreToZero() {
    Claim claim =
        buildClaim(
            Platform.PLATFORM_AMAZON, "ORD-999", 20260615, BigInteger.valueOf(50000), "john.doe");
    Map<String, ScoredValue> input =
        details("PLATFORM_AMAZON", "ORD-123", "2026-06-15", "500.00", "john.doe");

    ExtractedScoredResult result =
        ClaimScreenshotScorerUtils.updateExtractedDataForMatchWithManualEntryInOrder(
            claim, input, 90);

    assertTrue(result.extractedResult().get(BuzzmahConstants.ORDER_ID).isMismatch());
    assertEquals(0, result.extractedResult().get(BuzzmahConstants.ORDER_ID).getScore());
    assertEquals(0, result.overallScore());
  }

  @Test
  void missingOrderIdInExtractedDetailsNoScorePenalty() {
    // Rating/review/return screenshots don't extract orderId — absence should not penalize score.
    Claim claim =
        buildClaim(
            Platform.PLATFORM_AMAZON, "ORD-123", 20260615, BigInteger.valueOf(50000), "john.doe");
    Map<String, ScoredValue> input =
        details("PLATFORM_AMAZON", null, "2026-06-15", "500.00", "john.doe");

    ExtractedScoredResult result =
        ClaimScreenshotScorerUtils.updateExtractedDataForMatchWithManualEntryInOrder(
            claim, input, 90);

    assertFalse(result.extractedResult().get(BuzzmahConstants.ORDER_ID).isMismatch());
    assertEquals(90, result.overallScore());
  }

  @Test
  void nullAmountAndAccountNameOnClaimMarkedAsMismatchNoNpe() {
    Claim claim = buildClaim(Platform.PLATFORM_AMAZON, "ORD-123", 20260615, null, null);
    Map<String, ScoredValue> input =
        details("PLATFORM_AMAZON", "ORD-123", "2026-06-15", "500.00", "john.doe");

    ExtractedScoredResult result =
        ClaimScreenshotScorerUtils.updateExtractedDataForMatchWithManualEntryInOrder(
            claim, input, 90);

    assertTrue(result.extractedResult().get(BuzzmahConstants.AMOUNT).isMismatch());
    assertTrue(result.extractedResult().get(BuzzmahConstants.ORDERED_BY).isMismatch());
    // orderId still matches, so score is not forced to 0
    assertEquals(90, result.overallScore());
  }

  @Test
  void nullExtractedDetailsReturnsEnrichedMapWithAllMismatches() {
    Claim claim =
        buildClaim(
            Platform.PLATFORM_AMAZON, "ORD-123", 20260615, BigInteger.valueOf(50000), "john.doe");

    ExtractedScoredResult result =
        ClaimScreenshotScorerUtils.updateExtractedDataForMatchWithManualEntryInOrder(
            claim, null, 90);

    assertTrue(result.extractedResult().containsKey(BuzzmahConstants.PLATFORM));
    assertTrue(result.extractedResult().containsKey(BuzzmahConstants.ORDER_ID));
    assertTrue(result.extractedResult().containsKey(BuzzmahConstants.ORDER_DATE));
    assertTrue(result.extractedResult().containsKey(BuzzmahConstants.AMOUNT));
    assertTrue(result.extractedResult().containsKey(BuzzmahConstants.ORDERED_BY));
  }

  @Test
  void immutableInputMapDoesNotThrow() {
    Claim claim =
        buildClaim(
            Platform.PLATFORM_AMAZON, "ORD-123", 20260615, BigInteger.valueOf(50000), "john.doe");
    Map<String, ScoredValue> immutable =
        Map.of(BuzzmahConstants.ORDER_ID, ScoredValue.builder().extractedValue("ORD-123").build());

    assertDoesNotThrow(
        () ->
            ClaimScreenshotScorerUtils.updateExtractedDataForMatchWithManualEntryInOrder(
                claim, immutable, 90));
  }
}
