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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class RatingScreenshotScorer extends SimpleScoreApiScorer {

  public RatingScreenshotScorer(final ScoreApiClientProxy scoreApiClientProxy) {
    super(scoreApiClientProxy);
  }

  @Override
  protected String datasetKey() {
    return ScoreDatasetKeys.RATING;
  }

  @Override
  protected List<PayloadItem> additionalPayloadItems(
      final Claim claim, final Campaign campaign, final Map<String, ScoredValue> details) {
    return List.of(
        ClaimScreenshotScorerUtils.payloadItem(
            BuzzmahConstants.ACCOUNT_NAME,
            claim.getAccountName(),
            ClaimScreenshotScorerUtils.valueOf(details, BuzzmahConstants.ACCOUNT_NAME)));
  }

  @Override
  protected ExtractedScoredResult reconcile(
      final Claim claim, final Map<String, ScoredValue> details, final Integer overallScore) {
    final String rating = details.get("rating").getExtractedValue();
    // TODO Need to revisit as rating is compared against hard-coded value
    details.put(
        "rating",
        ScoredValue.builder()
            .extractedValue(rating)
            .score(StringUtils.isNumeric(rating) && Integer.parseInt(rating) >= 4 ? 100 : 0)
            .build());

    return ClaimScreenshotScorerUtils.updateExtractedDataForMatchWithManualEntryInRating(
        claim, details, overallScore);
  }
}
