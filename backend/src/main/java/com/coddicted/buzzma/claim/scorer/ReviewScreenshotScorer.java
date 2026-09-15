package com.coddicted.buzzma.claim.scorer;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.claim.client.ExtractedScoredResult;
import com.coddicted.buzzma.claim.client.ScoreApiClientProxy;
import com.coddicted.buzzma.claim.client.ScoreDatasetKeys;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.utils.ClaimScreenshotScorerUtils;
import com.coddicted.buzzma.extraction.entity.ScoredValue;
import com.coddicted.buzzma.shared.constants.BuzzmahConstants;
import com.coddicted.buzzma.shared.score.PayloadItem;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ReviewScreenshotScorer extends SimpleScoreApiScorer {

  public ReviewScreenshotScorer(final ScoreApiClientProxy scoreApiClientProxy) {
    super(scoreApiClientProxy);
  }

  @Override
  protected String datasetKey() {
    return ScoreDatasetKeys.REVIEW;
  }

  @Override
  protected List<PayloadItem> additionalPayloadItems(
      final Claim claim, final Campaign campaign, final Map<String, ScoredValue> details) {
    final String accountName =
        ClaimScreenshotScorerUtils.valueOf(details, BuzzmahConstants.ACCOUNT_NAME);
    final String reviewUrl =
        ClaimScreenshotScorerUtils.valueOf(details, BuzzmahConstants.REVIEW_URL);
    return reviewUrl != null
        ? List.of(
            ClaimScreenshotScorerUtils.payloadItem(
                BuzzmahConstants.ACCOUNT_NAME, claim.getAccountName(), accountName),
            ClaimScreenshotScorerUtils.payloadItem(
                BuzzmahConstants.REVIEW_URL, claim.getReviewUrl(), reviewUrl))
        : List.of(
            ClaimScreenshotScorerUtils.payloadItem(
                BuzzmahConstants.ACCOUNT_NAME, claim.getAccountName(), accountName));
  }

  @Override
  protected ExtractedScoredResult reconcile(
      final Claim claim, final Map<String, ScoredValue> details, final Integer overallScore) {
    return ClaimScreenshotScorerUtils.updateExtractedDataForMatchWithManualEntryInReview(
        claim, details, overallScore);
  }
}
