package com.coddicted.buzzma.claim.scorer;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.claim.client.ExtractedScoredResult;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import org.springframework.stereotype.Component;

@Component
public class CommentScreenshotScorer implements ClaimScreenshotScorer {

  @Override
  public ExtractedScoredResult score(
      final Claim claim, final Campaign campaign, final ClaimScreenshot screenshot) {
    return new ExtractedScoredResult(screenshot.getExtractedDetails(), 0);
  }
}
