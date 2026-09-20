package com.coddicted.buzzma.claim.scorer;

import static com.coddicted.buzzma.claim.scorer.Fixtures.CAMPAIGN;
import static com.coddicted.buzzma.claim.scorer.Fixtures.CLAIM;
import static com.coddicted.buzzma.claim.scorer.Fixtures.SCREENSHOT_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.claim.client.ExtractedScoredResult;
import com.coddicted.buzzma.claim.client.ScoreApiClientProxy;
import com.coddicted.buzzma.claim.client.ScoreDatasetKeys;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.entity.ScreenshotType;
import com.coddicted.buzzma.extraction.entity.ScoredValue;
import com.coddicted.buzzma.shared.constants.BuzzmahConstants;
import com.coddicted.buzzma.shared.score.PayloadItem;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeliveryScreenshotScorerTest {

  private static final List<PayloadItem> EXPECTED_PAYLOAD =
      Fixtures.loadPayload("/fixtures/output/claim/scorer/delivery-payload.json");

  @Mock private ScoreApiClientProxy mockScoreApiClientProxy;

  private DeliveryScreenshotScorer scorer() {
    return new DeliveryScreenshotScorer(this.mockScoreApiClientProxy);
  }

  private ExtractedScoredResult apiScoring(final int overallScore) {
    return new ExtractedScoredResult(
        Map.of(
            BuzzmahConstants.PLATFORM,
                ScoredValue.builder().extractedValue("PLATFORM_AMAZON").score(90).build(),
            BuzzmahConstants.PRODUCT_NAME,
                ScoredValue.builder().extractedValue("Test Product").score(90).build()),
        overallScore);
  }

  @Test
  void testScoreAppliesApiScoringAndHardFailChecks() {
    final ClaimScreenshot screenshot =
        ClaimScreenshot.builder()
            .id(SCREENSHOT_ID)
            .claimId(CLAIM.getId())
            .createdBy(CLAIM.getOwnerId())
            .type(ScreenshotType.SCREENSHOT_TYPE_DELIVERY)
            .extractedDetails(
                Fixtures.loadExtractedDetails(
                    "/fixtures/output/claim/processor/delivery-extracted-details.json"))
            .build();
    when(this.mockScoreApiClientProxy.score(ScoreDatasetKeys.DELIVERY, EXPECTED_PAYLOAD))
        .thenReturn(apiScoring(90));

    final ExtractedScoredResult result = scorer().score(CLAIM, CAMPAIGN, screenshot);

    assertEquals(90, result.overallScore());
    assertEquals(
        Fixtures.loadExtractedDetails(
            "/fixtures/output/claim/scorer/delivery-extracted-details.json"),
        result.extractedResult());
  }

  @Test
  void testOrderIdMismatchForcesScoreToZero() {
    final Map<String, ScoredValue> details =
        new HashMap<>(
            Fixtures.loadExtractedDetails(
                "/fixtures/output/claim/processor/delivery-extracted-details.json"));
    details.put(
        BuzzmahConstants.ORDER_ID,
        ScoredValue.builder().extractedValue("999-0000000-0000000").build());
    final ClaimScreenshot screenshot =
        ClaimScreenshot.builder()
            .id(SCREENSHOT_ID)
            .claimId(CLAIM.getId())
            .createdBy(CLAIM.getOwnerId())
            .type(ScreenshotType.SCREENSHOT_TYPE_DELIVERY)
            .extractedDetails(details)
            .build();
    when(this.mockScoreApiClientProxy.score(ScoreDatasetKeys.DELIVERY, EXPECTED_PAYLOAD))
        .thenReturn(apiScoring(90));

    final ExtractedScoredResult result = scorer().score(CLAIM, CAMPAIGN, screenshot);

    assertEquals(0, result.overallScore());
    assertTrue(result.extractedResult().get(BuzzmahConstants.ORDER_ID).isMismatch());
  }

  @Test
  void testOrderedByMismatchForcesScoreToZero() {
    final Map<String, ScoredValue> details =
        new HashMap<>(
            Fixtures.loadExtractedDetails(
                "/fixtures/output/claim/processor/delivery-extracted-details.json"));
    details.put(
        BuzzmahConstants.ORDERED_BY, ScoredValue.builder().extractedValue("Jane Smith").build());
    final ClaimScreenshot screenshot =
        ClaimScreenshot.builder()
            .id(SCREENSHOT_ID)
            .claimId(CLAIM.getId())
            .createdBy(CLAIM.getOwnerId())
            .type(ScreenshotType.SCREENSHOT_TYPE_DELIVERY)
            .extractedDetails(details)
            .build();
    when(this.mockScoreApiClientProxy.score(ScoreDatasetKeys.DELIVERY, EXPECTED_PAYLOAD))
        .thenReturn(apiScoring(90));

    final ExtractedScoredResult result = scorer().score(CLAIM, CAMPAIGN, screenshot);

    assertEquals(0, result.overallScore());
    assertTrue(result.extractedResult().get(BuzzmahConstants.ORDERED_BY).isMismatch());
  }

  @Test
  void testMissingOrderIdAndOrderedByDoNotHardFail() {
    final Map<String, ScoredValue> details =
        new HashMap<>(
            Fixtures.loadExtractedDetails(
                "/fixtures/output/claim/processor/delivery-extracted-details.json"));
    details.remove(BuzzmahConstants.ORDER_ID);
    details.remove(BuzzmahConstants.ORDERED_BY);
    final ClaimScreenshot screenshot =
        ClaimScreenshot.builder()
            .id(SCREENSHOT_ID)
            .claimId(CLAIM.getId())
            .createdBy(CLAIM.getOwnerId())
            .type(ScreenshotType.SCREENSHOT_TYPE_DELIVERY)
            .extractedDetails(details)
            .build();
    when(this.mockScoreApiClientProxy.score(ScoreDatasetKeys.DELIVERY, EXPECTED_PAYLOAD))
        .thenReturn(apiScoring(90));

    final ExtractedScoredResult result = scorer().score(CLAIM, CAMPAIGN, screenshot);

    assertEquals(90, result.overallScore());
    assertFalse(result.extractedResult().get(BuzzmahConstants.ORDER_ID).isMismatch());
    assertFalse(result.extractedResult().get(BuzzmahConstants.ORDERED_BY).isMismatch());
  }
}
