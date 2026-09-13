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
public class ReturnScreenshotScorer implements ClaimScreenshotScorer {

  private final ScoreApiClientProxy scoreApiClientProxy;

  public ReturnScreenshotScorer(final ScoreApiClientProxy scoreApiClientProxy) {
    this.scoreApiClientProxy = scoreApiClientProxy;
  }

  @Override
  public ExtractedScoredResult score(
      final Claim claim, final Campaign campaign, final ClaimScreenshot screenshot) {
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

    return ClaimScreenshotScorerUtils.updateExtractedDataForMatchWithManualEntryInReturn(
        claim, details, scoring.overallScore());
  }
}
