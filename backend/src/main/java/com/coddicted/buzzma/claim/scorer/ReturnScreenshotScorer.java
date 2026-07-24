package com.coddicted.buzzma.claim.scorer;

import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_RETURN;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ReturnScreenshotScorer implements ClaimScreenshotScorer {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReturnScreenshotScorer.class);

  private final ClaimScreenshotRepository screenshotRepository;
  private final CampaignService campaignService;
  private final ScoreApiClientProxy scoreApiClientProxy;
  private final ClaimService claimService;

  public ReturnScreenshotScorer(
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
    return SCREENSHOT_TYPE_RETURN == screenshot.getType();
  }

  @Override
  public void score(final ScoringJob job, final ClaimScreenshot screenshot) {
    LOGGER.info(
        "scoreReturnScreenshot: scoring job {}, screenshot {}", job.getId(), screenshot.getId());

    final Claim claim =
        this.claimService.getById(screenshot.getClaimId(), screenshot.getCreatedBy());
    final Campaign campaign = this.campaignService.getById(claim.getCampaignId());
    final Map<String, ScoredValue> details = new HashMap<>(screenshot.getExtractedDetails());

    final String platform = details.get(BuzzmahConstants.PLATFORM).getExtractedValue();
    final String productName = details.get(BuzzmahConstants.PRODUCT_NAME).getExtractedValue();
    final String accountName = details.get(BuzzmahConstants.ACCOUNT_NAME).getExtractedValue();

    final List<PayloadItem> payload =
        ClaimScreenshotScorerUtils.buildPayload(
            platform,
            productName,
            campaign,
            List.of(
                ClaimScreenshotScorerUtils.payloadItem(
                    BuzzmahConstants.ACCOUNT_NAME, claim.getAccountName(), accountName)));
    final ExtractedScoredResult scoring =
        this.scoreApiClientProxy.score(ScoreDatasetKeys.RETURN, payload);

    details.putAll(scoring.extractedResult());

    final ExtractedScoredResult extractedScoredResult =
        ClaimScreenshotScorerUtils.updateExtractedDataForMatchWithManualEntryInReturn(
            claim, details, scoring.overallScore());

    screenshot.setExtractedDetails(extractedScoredResult.extractedResult());
    screenshot.setScore(extractedScoredResult.overallScore());
    this.screenshotRepository.save(screenshot);

    this.claimService.updateClaimScore(screenshot.getClaimId());
    LOGGER.info("scoreReturnScreenshot: saved score for screenshot {}", screenshot.getId());
  }
}
