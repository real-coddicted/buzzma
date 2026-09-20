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
public class SellerFeedbackScreenshotScorer extends SimpleScoreApiScorer {

  public SellerFeedbackScreenshotScorer(final ScoreApiClientProxy scoreApiClientProxy) {
    super(scoreApiClientProxy);
  }

  @Override
  protected String datasetKey() {
    return ScoreDatasetKeys.SELLER_FEEDBACK;
  }

  @Override
  protected List<PayloadItem> additionalPayloadItems(
      final Claim claim, final Campaign campaign, final Map<String, ScoredValue> details) {
    return List.of(
        ClaimScreenshotScorerUtils.payloadItem(
            BuzzmahConstants.SELLER_NAME,
            campaign.getSellerName(),
            ClaimScreenshotScorerUtils.valueOf(details, BuzzmahConstants.SELLER_NAME)));
  }

  @Override
  protected ExtractedScoredResult reconcile(
      final Claim claim, final Map<String, ScoredValue> details, final Integer overallScore) {
    final String rating = details.get("rating").getExtractedValue();
    details.put(
        "rating",
        ScoredValue.builder()
            .extractedValue(rating)
            .score(StringUtils.isNumeric(rating) && Integer.parseInt(rating) >= 4 ? 100 : 0)
            .build());

    return ClaimScreenshotScorerUtils.updateExtractedDataForMatchWithManualEntryInSellerFeedback(
        claim, details, overallScore);
  }
}
