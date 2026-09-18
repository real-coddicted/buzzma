package com.coddicted.buzzma.claim.scorer;

import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_DELIVERY;
import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_ORDER;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeliveryScreenshotScorerTest {

  private static final List<PayloadItem> EXPECTED_PAYLOAD =
      Fixtures.loadPayload("/fixtures/output/claim/scorer/delivery-payload.json");

  @Mock private ClaimScreenshotRepository mockScreenshotRepository;
  @Mock private CampaignService mockCampaignService;
  @Mock private ClaimService mockClaimService;
  @Mock private ScoreApiClientProxy mockScoreApiClientProxy;

  private DeliveryScreenshotScorer scorer() {
    return new DeliveryScreenshotScorer(
        this.mockScreenshotRepository,
        this.mockCampaignService,
        this.mockScoreApiClientProxy,
        this.mockClaimService);
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
  void testCanScoreOnlyMatchesDeliveryScreenshots() {
    assertTrue(scorer().canScore(ClaimScreenshot.builder().type(SCREENSHOT_TYPE_DELIVERY).build()));
    assertFalse(scorer().canScore(ClaimScreenshot.builder().type(SCREENSHOT_TYPE_ORDER).build()));
  }

  @Test
  void testScoreAppliesApiScoringAndHardFailChecks() {
    final ClaimScreenshot screenshot =
        ClaimScreenshot.builder()
            .id(SCREENSHOT_ID)
            .claimId(CLAIM.getId())
            .createdBy(CLAIM.getOwnerId())
            .type(SCREENSHOT_TYPE_DELIVERY)
            .extractedDetails(
                Fixtures.loadExtractedDetails(
                    "/fixtures/output/claim/processor/delivery-extracted-details.json"))
            .build();
    when(this.mockClaimService.getById(CLAIM.getId(), CLAIM.getOwnerId())).thenReturn(CLAIM);
    when(this.mockCampaignService.getById(CAMPAIGN.getId())).thenReturn(CAMPAIGN);
    when(this.mockScoreApiClientProxy.score(ScoreDatasetKeys.DELIVERY, EXPECTED_PAYLOAD))
        .thenReturn(apiScoring(90));

    scorer()
        .score(
            ScoringJob.builder().id(JOB_ID).claimScreenshotId(SCREENSHOT_ID).build(), screenshot);

    final ArgumentCaptor<ClaimScreenshot> captor = ArgumentCaptor.forClass(ClaimScreenshot.class);
    verify(this.mockScreenshotRepository).save(captor.capture());
    final ClaimScreenshot saved = captor.getValue();

    assertEquals(90, saved.getScore());
    assertEquals(
        Fixtures.loadExtractedDetails(
            "/fixtures/output/claim/scorer/delivery-extracted-details.json"),
        saved.getExtractedDetails());

    verify(this.mockClaimService).updateClaimScore(CLAIM.getId());
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
            .type(SCREENSHOT_TYPE_DELIVERY)
            .extractedDetails(details)
            .build();
    when(this.mockClaimService.getById(CLAIM.getId(), CLAIM.getOwnerId())).thenReturn(CLAIM);
    when(this.mockCampaignService.getById(CAMPAIGN.getId())).thenReturn(CAMPAIGN);
    when(this.mockScoreApiClientProxy.score(ScoreDatasetKeys.DELIVERY, EXPECTED_PAYLOAD))
        .thenReturn(apiScoring(90));

    scorer()
        .score(
            ScoringJob.builder().id(JOB_ID).claimScreenshotId(SCREENSHOT_ID).build(), screenshot);

    final ArgumentCaptor<ClaimScreenshot> captor = ArgumentCaptor.forClass(ClaimScreenshot.class);
    verify(this.mockScreenshotRepository).save(captor.capture());
    final ClaimScreenshot saved = captor.getValue();

    assertEquals(0, saved.getScore());
    assertTrue(saved.getExtractedDetails().get(BuzzmahConstants.ORDER_ID).isMismatch());
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
            .type(SCREENSHOT_TYPE_DELIVERY)
            .extractedDetails(details)
            .build();
    when(this.mockClaimService.getById(CLAIM.getId(), CLAIM.getOwnerId())).thenReturn(CLAIM);
    when(this.mockCampaignService.getById(CAMPAIGN.getId())).thenReturn(CAMPAIGN);
    when(this.mockScoreApiClientProxy.score(ScoreDatasetKeys.DELIVERY, EXPECTED_PAYLOAD))
        .thenReturn(apiScoring(90));

    scorer()
        .score(
            ScoringJob.builder().id(JOB_ID).claimScreenshotId(SCREENSHOT_ID).build(), screenshot);

    final ArgumentCaptor<ClaimScreenshot> captor = ArgumentCaptor.forClass(ClaimScreenshot.class);
    verify(this.mockScreenshotRepository).save(captor.capture());
    final ClaimScreenshot saved = captor.getValue();

    assertEquals(0, saved.getScore());
    assertTrue(saved.getExtractedDetails().get(BuzzmahConstants.ORDERED_BY).isMismatch());
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
            .type(SCREENSHOT_TYPE_DELIVERY)
            .extractedDetails(details)
            .build();
    when(this.mockClaimService.getById(CLAIM.getId(), CLAIM.getOwnerId())).thenReturn(CLAIM);
    when(this.mockCampaignService.getById(CAMPAIGN.getId())).thenReturn(CAMPAIGN);
    when(this.mockScoreApiClientProxy.score(ScoreDatasetKeys.DELIVERY, EXPECTED_PAYLOAD))
        .thenReturn(apiScoring(90));

    scorer()
        .score(
            ScoringJob.builder().id(JOB_ID).claimScreenshotId(SCREENSHOT_ID).build(), screenshot);

    final ArgumentCaptor<ClaimScreenshot> captor = ArgumentCaptor.forClass(ClaimScreenshot.class);
    verify(this.mockScreenshotRepository).save(captor.capture());
    final ClaimScreenshot saved = captor.getValue();

    assertEquals(90, saved.getScore());
    assertFalse(saved.getExtractedDetails().get(BuzzmahConstants.ORDER_ID).isMismatch());
    assertFalse(saved.getExtractedDetails().get(BuzzmahConstants.ORDERED_BY).isMismatch());
  }
}
