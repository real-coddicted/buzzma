package com.coddicted.buzzma.claim.scorer;

import static com.coddicted.buzzma.claim.scorer.Fixtures.CAMPAIGN;
import static com.coddicted.buzzma.claim.scorer.Fixtures.CLAIM;
import static com.coddicted.buzzma.claim.scorer.Fixtures.SCREENSHOT_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.claim.client.ExtractedScoredResult;
import com.coddicted.buzzma.claim.client.ScoreApiClientProxy;
import com.coddicted.buzzma.claim.client.ScoreDatasetKeys;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.entity.ScreenshotType;
import com.coddicted.buzzma.extraction.entity.ScoredValue;
import com.coddicted.buzzma.shared.constants.BuzzmahConstants;
import com.coddicted.buzzma.shared.score.PayloadItem;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReturnScreenshotScorerTest {

  private static final List<PayloadItem> EXPECTED_PAYLOAD =
      Fixtures.loadPayload("/fixtures/output/claim/scorer/return-payload.json");

  @Mock private ScoreApiClientProxy mockScoreApiClientProxy;

  @Test
  void testScoreDoesNotTouchRatingFieldAndSavesMismatchFlags() {
    final ReturnScreenshotScorer scorer = new ReturnScreenshotScorer(this.mockScoreApiClientProxy);

    final ClaimScreenshot screenshot =
        ClaimScreenshot.builder()
            .id(SCREENSHOT_ID)
            .claimId(CLAIM.getId())
            .createdBy(CLAIM.getOwnerId())
            .type(ScreenshotType.SCREENSHOT_TYPE_RETURN)
            .extractedDetails(
                Fixtures.loadExtractedDetails(
                    "/fixtures/output/claim/processor/return-extracted-details.json"))
            .build();
    when(this.mockScoreApiClientProxy.score(ScoreDatasetKeys.RETURN, EXPECTED_PAYLOAD))
        .thenReturn(
            new ExtractedScoredResult(
                Map.of(
                    BuzzmahConstants.PLATFORM,
                        ScoredValue.builder().extractedValue("PLATFORM_AMAZON").score(100).build(),
                    BuzzmahConstants.PRODUCT_NAME,
                        ScoredValue.builder().extractedValue("Test Product").score(100).build(),
                    BuzzmahConstants.ACCOUNT_NAME,
                        ScoredValue.builder().extractedValue("john.doe").score(80).build()),
                85));

    // This screenshot type never extracts a "rating" field; regression coverage for the bug where
    // ReturnScreenshotScorer previously read details.get("rating") and threw an NPE.
    final ExtractedScoredResult result = scorer.score(CLAIM, CAMPAIGN, screenshot);

    assertEquals(85, result.overallScore());
    assertEquals(
        Fixtures.loadExtractedDetails(
            "/fixtures/output/claim/scorer/return-extracted-details.json"),
        result.extractedResult());
    assertFalse(result.extractedResult().containsKey("rating"));
  }
}
