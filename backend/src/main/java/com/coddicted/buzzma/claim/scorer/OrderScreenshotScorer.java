package com.coddicted.buzzma.claim.scorer;

import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_ORDER;

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
public class OrderScreenshotScorer implements ClaimScreenshotScorer {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrderScreenshotScorer.class);

  private final ClaimScreenshotRepository screenshotRepository;
  private final CampaignService campaignService;
  private final ClaimService claimService;
  private final ScoreApiClientProxy scoreApiClientProxy;

  public OrderScreenshotScorer(
      final ClaimScreenshotRepository screenshotRepository,
      final CampaignService campaignService,
      final ClaimService claimService,
      final ScoreApiClientProxy scoreApiClientProxy) {
    this.screenshotRepository = screenshotRepository;
    this.campaignService = campaignService;
    this.claimService = claimService;
    this.scoreApiClientProxy = scoreApiClientProxy;
  }

  @Override
  public boolean canScore(final ClaimScreenshot screenshot) {
    return SCREENSHOT_TYPE_ORDER == screenshot.getType();
  }

  @Override
  public void score(final ScoringJob job, final ClaimScreenshot screenshot) {
    LOGGER.info(
        "scoreOrderScreenshot: scoring job {}, screenshot {}", job.getId(), screenshot.getId());

    final Claim claim =
        this.claimService.getById(screenshot.getClaimId(), screenshot.getCreatedBy());
    final Campaign campaign = this.campaignService.getById(claim.getCampaignId());
    final Map<String, ScoredValue> details = new HashMap<>(screenshot.getExtractedDetails());

    final String orderDate = details.get(BuzzmahConstants.ORDER_DATE).getExtractedValue();
    final String platformValue = details.get(BuzzmahConstants.PLATFORM).getExtractedValue();
    final String productName = details.get(BuzzmahConstants.PRODUCT_NAME).getExtractedValue();
    final String sellerName = details.get(BuzzmahConstants.SELLER_NAME).getExtractedValue();

    final ExtractedScoredResult fieldScoring =
        scoreFields(platformValue, productName, sellerName, orderDate, campaign);
    details.putAll(fieldScoring.extractedResult());

    final ExtractedScoredResult extractedScoredResult =
        ClaimScreenshotScorerUtils.updateExtractedDataForMatchWithManualEntryInOrder(
            claim, details, fieldScoring.overallScore());

    screenshot.setExtractedDetails(extractedScoredResult.extractedResult());
    screenshot.setScore(extractedScoredResult.overallScore());
    this.screenshotRepository.save(screenshot);

    this.claimService.updateClaimScore(screenshot.getClaimId());

    LOGGER.info("scoreOrderScreenshot: saved score for screenshot {}", screenshot.getId());
  }

  /**
   * Scores the platform/productName/sellerName fields of an order screenshot against the campaign
   * via the Score API, plus the locally-computed orderDate score, and combines them into a single
   * overall score. Exposed for reuse by the synchronous extract-and-score preview
   * (ClaimScreenshotServiceImpl.extractSync), which scores the same raw fields before a Claim
   * exists.
   */
  public ExtractedScoredResult scoreFields(
      final String platform,
      final String productName,
      final String sellerName,
      final String orderDate,
      final Campaign campaign) {

    final int orderDateScore =
        (int) Math.round(ClaimScreenshotScorerUtils.scoreOrderDate(orderDate, campaign) * 100);

    final List<PayloadItem> payload =
        ClaimScreenshotScorerUtils.buildPayload(
            platform,
            productName,
            campaign,
            List.of(
                ClaimScreenshotScorerUtils.payloadItem(
                    BuzzmahConstants.SELLER_NAME, campaign.getSellerName(), sellerName)));

    final ExtractedScoredResult apiScoring =
        this.scoreApiClientProxy.score(ScoreDatasetKeys.ORDER, payload);

    final Map<String, ScoredValue> details = new HashMap<>(apiScoring.extractedResult());
    details.put(
        BuzzmahConstants.ORDER_DATE,
        ScoredValue.builder().extractedValue(orderDate).score(orderDateScore).build());

    final int overallScore =
        ClaimScreenshotScorerUtils.combineOverallScore(
            orderDateScore, apiScoring.extractedResult());
    return new ExtractedScoredResult(details, overallScore);
  }
}
