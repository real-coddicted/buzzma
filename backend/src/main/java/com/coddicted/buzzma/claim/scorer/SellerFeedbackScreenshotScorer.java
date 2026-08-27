package com.coddicted.buzzma.claim.scorer;

import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_SELLER_FEEDBACK;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.claim.client.ExtractedScoredResult;
import com.coddicted.buzzma.claim.client.ScoreApiClientProxy;
import com.coddicted.buzzma.claim.client.ScoreDatasetKeys;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.persistence.ClaimScreenshotRepository;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.claim.utils.ClaimScreenshotScorerUtils;
import com.coddicted.buzzma.extraction.entity.ScoredValue;
import com.coddicted.buzzma.scoring.entity.ScoringJob;
import com.coddicted.buzzma.shared.constants.BuzzmahConstants;
import com.coddicted.buzzma.shared.score.PayloadItem;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SellerFeedbackScreenshotScorer implements ClaimScreenshotScorer {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(SellerFeedbackScreenshotScorer.class);

  private final ClaimScreenshotRepository screenshotRepository;
  private final CampaignService campaignService;
  private final ScoreApiClientProxy scoreApiClientProxy;
  private final ClaimService claimService;

  public SellerFeedbackScreenshotScorer(
      final ClaimScreenshotRepository screenshotRepository,
      final CampaignService campaignService,
      final ScoreApiClientProxy scoreApiClientProxy,
      final ClaimService claimService) {
    this.screenshotRepository = screenshotRepository;
    this.campaignService = campaignService;
    this.scoreApiClientProxy = scoreApiClientProxy;
    this.claimService = claimService;
  }

  @Override
  public boolean canScore(final ClaimScreenshot screenshot) {
    return SCREENSHOT_TYPE_SELLER_FEEDBACK == screenshot.getType();
  }

  @Override
  public void score(final ScoringJob job, final ClaimScreenshot screenshot) {
    LOGGER.info(
        "scoreSellerFeedbackScreenshot: scoring job {}, screenshot {}",
        job.getId(),
        screenshot.getId());

    final Claim claim =
        this.claimService.getById(screenshot.getClaimId(), screenshot.getCreatedBy());
    final Campaign campaign = this.campaignService.getById(claim.getCampaignId());
    final Map<String, ScoredValue> details = new HashMap<>(screenshot.getExtractedDetails());

    final String platform = details.get(BuzzmahConstants.PLATFORM).getExtractedValue();
    final String productName = details.get(BuzzmahConstants.PRODUCT_NAME).getExtractedValue();
    final String sellerName = details.get(BuzzmahConstants.SELLER_NAME).getExtractedValue();
    final List<PayloadItem> payload =
        ClaimScreenshotScorerUtils.buildPayload(
            platform,
            productName,
            campaign,
            List.of(
                ClaimScreenshotScorerUtils.payloadItem(
                    BuzzmahConstants.SELLER_NAME, campaign.getSellerName(), sellerName)));
    final ExtractedScoredResult scoring =
        this.scoreApiClientProxy.score(ScoreDatasetKeys.SELLER_FEEDBACK, payload);
    details.putAll(scoring.extractedResult());

    final String rating = details.get("rating").getExtractedValue();
    details.put(
        "rating",
        ScoredValue.builder()
            .extractedValue(rating)
            .score(StringUtils.isNumeric(rating) && Integer.parseInt(rating) >= 4 ? 100 : 0)
            .build());

    final ExtractedScoredResult extractedScoredResult =
        ClaimScreenshotScorerUtils.updateExtractedDataForMatchWithManualEntryInSellerFeedback(
            claim, details, scoring.overallScore());

    screenshot.setExtractedDetails(extractedScoredResult.extractedResult());
    screenshot.setScore(extractedScoredResult.overallScore());
    this.screenshotRepository.save(screenshot);

    this.claimService.updateClaimScore(screenshot.getClaimId());

    LOGGER.info("scoreSellerFeedbackScreenshot: saved score for screenshot {}", screenshot.getId());
  }
}
