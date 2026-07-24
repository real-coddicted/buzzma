package com.coddicted.buzzma.claim.scorer;

import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_ORDER;
import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_REVIEW;
import static com.coddicted.buzzma.claim.scorer.Fixtures.CAMPAIGN;
import static com.coddicted.buzzma.claim.scorer.Fixtures.CLAIM;
import static com.coddicted.buzzma.claim.scorer.Fixtures.JOB_ID;
import static com.coddicted.buzzma.claim.scorer.Fixtures.SCREENSHOT_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.claim.client.ExtractedScoredResult;
import com.coddicted.buzzma.claim.client.ScoreApiClientProxy;
import com.coddicted.buzzma.claim.client.ScoreDatasetKeys;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.persistence.ClaimScreenshotRepository;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.extraction.entity.ScoredValue;
import com.coddicted.buzzma.scoring.entity.ScoringJob;
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
class ReviewScreenshotScorerTest {

  private static final List<PayloadItem> EXPECTED_PAYLOAD =
      Fixtures.loadPayload("/fixtures/output/claim/scorer/review-payload.json");

  @Mock private ClaimScreenshotRepository mockScreenshotRepository;
  @Mock private CampaignService mockCampaignService;
  @Mock private ClaimService mockClaimService;
  @Mock private ScoreApiClientProxy mockScoreApiClientProxy;

  @Test
  void testCanScoreOnlyMatchesReviewScreenshots() {
    final ReviewScreenshotScorer scorer =
        new ReviewScreenshotScorer(
            this.mockScreenshotRepository,
            this.mockCampaignService,
            this.mockScoreApiClientProxy,
            this.mockClaimService);

    assertTrue(scorer.canScore(ClaimScreenshot.builder().type(SCREENSHOT_TYPE_REVIEW).build()));
    assertFalse(scorer.canScore(ClaimScreenshot.builder().type(SCREENSHOT_TYPE_ORDER).build()));
  }

  @Test
  void testScoreIncludesReviewUrlInPayloadAndSavesMismatchFlags() {
    final ReviewScreenshotScorer scorer =
        new ReviewScreenshotScorer(
            this.mockScreenshotRepository,
            this.mockCampaignService,
            this.mockScoreApiClientProxy,
            this.mockClaimService);

    final ClaimScreenshot screenshot =
        ClaimScreenshot.builder()
            .id(SCREENSHOT_ID)
            .claimId(CLAIM.getId())
            .createdBy(CLAIM.getOwnerId())
            .type(SCREENSHOT_TYPE_REVIEW)
            .extractedDetails(
                Fixtures.loadExtractedDetails(
                    "/fixtures/output/claim/processor/review-extracted-details.json"))
            .build();
    when(this.mockClaimService.getById(CLAIM.getId(), CLAIM.getOwnerId())).thenReturn(CLAIM);
    when(this.mockCampaignService.getById(CAMPAIGN.getId())).thenReturn(CAMPAIGN);
    when(this.mockScoreApiClientProxy.score(ScoreDatasetKeys.REVIEW, EXPECTED_PAYLOAD))
        .thenReturn(
            new ExtractedScoredResult(
                Map.of(
                    BuzzmahConstants.PLATFORM,
                        ScoredValue.builder().extractedValue("PLATFORM_AMAZON").score(100).build(),
                    BuzzmahConstants.PRODUCT_NAME,
                        ScoredValue.builder().extractedValue("Test Product").score(100).build(),
                    BuzzmahConstants.ACCOUNT_NAME,
                        ScoredValue.builder().extractedValue("john.doe").score(80).build(),
                    BuzzmahConstants.REVIEW_URL,
                        ScoredValue.builder()
                            .extractedValue("https://amazon.in/review/123")
                            .score(100)
                            .build()),
                90));

    scorer.score(
        ScoringJob.builder().id(JOB_ID).claimScreenshotId(SCREENSHOT_ID).build(), screenshot);

    final ArgumentCaptor<ClaimScreenshot> captor = ArgumentCaptor.forClass(ClaimScreenshot.class);
    verify(this.mockScreenshotRepository).save(captor.capture());
    final ClaimScreenshot saved = captor.getValue();

    assertEquals(90, saved.getScore());
    assertEquals(
        Fixtures.loadExtractedDetails(
            "/fixtures/output/claim/scorer/review-extracted-details.json"),
        saved.getExtractedDetails());

    verify(this.mockClaimService).updateClaimScore(CLAIM.getId());
  }
}
