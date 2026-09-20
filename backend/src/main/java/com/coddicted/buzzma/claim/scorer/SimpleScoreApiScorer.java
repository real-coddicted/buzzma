package com.coddicted.buzzma.claim.scorer;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.claim.client.ExtractedScoredResult;
import com.coddicted.buzzma.claim.client.ScoreApiClientProxy;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.utils.ClaimScreenshotScorerUtils;
import com.coddicted.buzzma.extraction.entity.ScoredValue;
import com.coddicted.buzzma.shared.constants.BuzzmahConstants;
import com.coddicted.buzzma.shared.score.PayloadItem;
import java.util.List;
import java.util.Map;

/**
 * Scorers that compute their score with a single Score API call: build a payload from
 * platform/productName plus whatever step-specific fields matter, submit it under the step's
 * dataset key, and use the API's result as-is.
 */
public abstract class SimpleScoreApiScorer extends AbstractScoreApiScorer {

  protected final ScoreApiClientProxy scoreApiClientProxy;

  protected SimpleScoreApiScorer(final ScoreApiClientProxy scoreApiClientProxy) {
    this.scoreApiClientProxy = scoreApiClientProxy;
  }

  @Override
  protected final ExtractedScoredResult computeScoring(
      final Claim claim, final Campaign campaign, final Map<String, ScoredValue> details) {
    final List<PayloadItem> payload =
        ClaimScreenshotScorerUtils.buildPayload(
            ClaimScreenshotScorerUtils.valueOf(details, BuzzmahConstants.PLATFORM),
            ClaimScreenshotScorerUtils.valueOf(details, BuzzmahConstants.PRODUCT_NAME),
            campaign,
            additionalPayloadItems(claim, campaign, details));
    return this.scoreApiClientProxy.score(datasetKey(), payload);
  }

  protected abstract String datasetKey();

  protected abstract List<PayloadItem> additionalPayloadItems(
      Claim claim, Campaign campaign, Map<String, ScoredValue> details);
}
