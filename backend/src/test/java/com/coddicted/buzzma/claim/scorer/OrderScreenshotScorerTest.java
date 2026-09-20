package com.coddicted.buzzma.claim.scorer;

import static com.coddicted.buzzma.claim.scorer.Fixtures.CAMPAIGN;
import static com.coddicted.buzzma.claim.scorer.Fixtures.CLAIM;
import static com.coddicted.buzzma.claim.scorer.Fixtures.SCREENSHOT_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.campaign.entity.Campaign;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderScreenshotScorerTest {

  private static final List<PayloadItem> EXPECTED_PAYLOAD =
      Fixtures.loadPayload("/fixtures/output/claim/scorer/order-payload.json");

  @Mock private ScoreApiClientProxy mockScoreApiClientProxy;

  @Test
  void testScoreSavesCombinedScoreAndMismatchFlags() {
    final OrderScreenshotScorer scorer = new OrderScreenshotScorer(this.mockScoreApiClientProxy);

    final ClaimScreenshot screenshot =
        ClaimScreenshot.builder()
            .id(SCREENSHOT_ID)
            .claimId(CLAIM.getId())
            .createdBy(CLAIM.getOwnerId())
            .type(ScreenshotType.SCREENSHOT_TYPE_ORDER)
            .extractedDetails(
                Fixtures.loadExtractedDetails(
                    "/fixtures/output/claim/processor/order-extracted-details.json"))
            .build();
    when(this.mockScoreApiClientProxy.score(ScoreDatasetKeys.ORDER, EXPECTED_PAYLOAD))
        .thenReturn(
            new ExtractedScoredResult(
                Map.of(
                    BuzzmahConstants.PLATFORM,
                        ScoredValue.builder().extractedValue("PLATFORM_AMAZON").score(100).build(),
                    BuzzmahConstants.PRODUCT_NAME,
                        ScoredValue.builder().extractedValue("Test Product").score(100).build(),
                    BuzzmahConstants.SELLER_NAME,
                        ScoredValue.builder().extractedValue("Acme Sellers").score(100).build()),
                95));

    final ExtractedScoredResult result = scorer.score(CLAIM, CAMPAIGN, screenshot);

    // Combined score is the min of orderDate (100) and the API's platform/productName/sellerName
    // scores (all 100), rather than the API's own overallScore (95).
    assertEquals(100, result.overallScore());
    assertEquals(
        Fixtures.loadExtractedDetails("/fixtures/output/claim/scorer/order-extracted-details.json"),
        result.extractedResult());
  }

  @Test
  @SuppressWarnings("unchecked")
  void testScoreExcludesSellerNameFromPayloadWhenCampaignHasNone() {
    final OrderScreenshotScorer scorer = new OrderScreenshotScorer(this.mockScoreApiClientProxy);

    final Campaign campaignNoSeller = CAMPAIGN.toBuilder().sellerName(null).build();
    final ClaimScreenshot screenshot =
        ClaimScreenshot.builder()
            .id(SCREENSHOT_ID)
            .claimId(CLAIM.getId())
            .createdBy(CLAIM.getOwnerId())
            .type(ScreenshotType.SCREENSHOT_TYPE_ORDER)
            .extractedDetails(
                Fixtures.loadExtractedDetails(
                    "/fixtures/output/claim/processor/order-extracted-details.json"))
            .build();
    when(this.mockScoreApiClientProxy.score(eq(ScoreDatasetKeys.ORDER), any()))
        .thenReturn(
            new ExtractedScoredResult(
                Map.of(
                    BuzzmahConstants.PLATFORM,
                        ScoredValue.builder().extractedValue("PLATFORM_AMAZON").score(100).build(),
                    BuzzmahConstants.PRODUCT_NAME,
                        ScoredValue.builder().extractedValue("Test Product").score(100).build()),
                100));

    scorer.score(CLAIM, campaignNoSeller, screenshot);

    final ArgumentCaptor<List<PayloadItem>> captor =
        ArgumentCaptor.forClass((Class<List<PayloadItem>>) (Class<?>) List.class);
    verify(this.mockScoreApiClientProxy).score(eq(ScoreDatasetKeys.ORDER), captor.capture());
    assertTrue(
        captor.getValue().stream()
            .noneMatch(p -> BuzzmahConstants.SELLER_NAME.equals(p.getLabel())));
  }
}
