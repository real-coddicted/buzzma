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
public class ReviewScreenshotScorer implements ClaimScreenshotScorer {

  private final ScoreApiClientProxy scoreApiClientProxy;

  public ReviewScreenshotScorer(final ScoreApiClientProxy scoreApiClientProxy) {
    this.scoreApiClientProxy = scoreApiClientProxy;
  }

  @Override
  public ExtractedScoredResult score(
      final Claim claim, final Campaign campaign, final ClaimScreenshot screenshot) {
    final Map<String, ScoredValue> details = new HashMap<>(screenshot.getExtractedDetails());

    final String platform = details.get(BuzzmahConstants.PLATFORM).getExtractedValue();
    final String productName = details.get(BuzzmahConstants.PRODUCT_NAME).getExtractedValue();
    final String accountName = details.get(BuzzmahConstants.ACCOUNT_NAME).getExtractedValue();
    final String reviewUrl = details.get(BuzzmahConstants.REVIEW_URL).getExtractedValue();
    final List<PayloadItem> additionalItems =
        reviewUrl != null
            ? List.of(
                ClaimScreenshotScorerUtils.payloadItem(
                    BuzzmahConstants.ACCOUNT_NAME, claim.getAccountName(), accountName),
                ClaimScreenshotScorerUtils.payloadItem(
                    BuzzmahConstants.REVIEW_URL, claim.getReviewUrl(), reviewUrl))
            : List.of(
                ClaimScreenshotScorerUtils.payloadItem(
                    BuzzmahConstants.ACCOUNT_NAME, claim.getAccountName(), accountName));
    final List<PayloadItem> payload =
        ClaimScreenshotScorerUtils.buildPayload(platform, productName, campaign, additionalItems);
    final ExtractedScoredResult scoring =
        this.scoreApiClientProxy.score(ScoreDatasetKeys.REVIEW, payload);
    details.putAll(scoring.extractedResult());

    return ClaimScreenshotScorerUtils.updateExtractedDataForMatchWithManualEntryInReview(
        claim, details, scoring.overallScore());
  }
}
