package com.coddicted.buzzma.claim.scorer;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.claim.client.ExtractedScoredResult;
import com.coddicted.buzzma.claim.client.ScoreApiClientProxy;
import com.coddicted.buzzma.claim.client.ScoreDatasetKeys;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.utils.ClaimScreenshotScorerUtils;
import com.coddicted.buzzma.extraction.entity.ScoredValue;
import com.coddicted.buzzma.shared.constants.BuzzmahConstants;
import com.coddicted.buzzma.shared.score.PayloadItem;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class OrderScreenshotScorer implements ClaimScreenshotScorer {

  private final ScoreApiClientProxy scoreApiClientProxy;

  public OrderScreenshotScorer(final ScoreApiClientProxy scoreApiClientProxy) {
    this.scoreApiClientProxy = scoreApiClientProxy;
  }

  @Override
  public ExtractedScoredResult score(
      final Claim claim, final Campaign campaign, final ClaimScreenshot screenshot) {
    final Map<String, ScoredValue> details = new HashMap<>(screenshot.getExtractedDetails());

    final String orderDate = details.get(BuzzmahConstants.ORDER_DATE).getExtractedValue();
    final String platformValue = details.get(BuzzmahConstants.PLATFORM).getExtractedValue();
    final String productName = details.get(BuzzmahConstants.PRODUCT_NAME).getExtractedValue();
    final ScoredValue sellerNameValue = details.get(BuzzmahConstants.SELLER_NAME);
    final String sellerName = sellerNameValue != null ? sellerNameValue.getExtractedValue() : null;

    final ExtractedScoredResult fieldScoring =
        scoreFields(platformValue, productName, sellerName, orderDate, campaign);
    details.putAll(fieldScoring.extractedResult());

    return ClaimScreenshotScorerUtils.updateExtractedDataForMatchWithManualEntryInOrder(
        claim, details, fieldScoring.overallScore());
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
            campaign.getSellerName() != null
                ? List.of(
                    ClaimScreenshotScorerUtils.payloadItem(
                        BuzzmahConstants.SELLER_NAME, campaign.getSellerName(), sellerName))
                : List.of());

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
