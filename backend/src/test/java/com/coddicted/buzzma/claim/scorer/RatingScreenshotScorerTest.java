package com.coddicted.buzzma.claim.scorer;

import static com.coddicted.buzzma.claim.scorer.Fixtures.CAMPAIGN;
import static com.coddicted.buzzma.claim.scorer.Fixtures.CLAIM;
import static com.coddicted.buzzma.claim.scorer.Fixtures.SCREENSHOT_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
class RatingScreenshotScorerTest {

  private static final List<PayloadItem> EXPECTED_PAYLOAD =
      Fixtures.loadPayload("/fixtures/output/claim/scorer/rating-payload.json");

  @Mock private ScoreApiClientProxy mockScoreApiClientProxy;

  @Test
  void testScoreAppliesHardCodedRatingThresholdAndSavesMismatchFlags() {
    final RatingScreenshotScorer scorer = new RatingScreenshotScorer(this.mockScoreApiClientProxy);

    final ClaimScreenshot screenshot =
        ClaimScreenshot.builder()
            .id(SCREENSHOT_ID)
            .claimId(CLAIM.getId())
            .createdBy(CLAIM.getOwnerId())
            .type(ScreenshotType.SCREENSHOT_TYPE_RATING)
            .extractedDetails(
                Fixtures.loadExtractedDetails(
                    "/fixtures/output/claim/processor/rating-extracted-details.json"))
            .build();
    when(this.mockScoreApiClientProxy.score(ScoreDatasetKeys.RATING, EXPECTED_PAYLOAD))
        .thenReturn(
            new ExtractedScoredResult(
                Map.of(
                    BuzzmahConstants.PLATFORM,
                        ScoredValue.builder().extractedValue("PLATFORM_AMAZON").score(100).build(),
                    BuzzmahConstants.PRODUCT_NAME,
                        ScoredValue.builder().extractedValue("Test Product").score(100).build(),
                    BuzzmahConstants.ACCOUNT_NAME,
                        ScoredValue.builder().extractedValue("john.doe").score(80).build()),
                90));

    final ExtractedScoredResult result = scorer.score(CLAIM, CAMPAIGN, screenshot);

    // Rating's overall score comes straight from the Score API (90); unlike Order, there is no
    // local score to combine it with.
    assertEquals(90, result.overallScore());
    assertEquals(
        Fixtures.loadExtractedDetails(
            "/fixtures/output/claim/scorer/rating-extracted-details.json"),
        result.extractedResult());
  }
}
