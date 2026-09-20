package com.coddicted.buzzma.claim.scorer;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.claim.client.ExtractedScoredResult;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import org.springframework.stereotype.Component;

/**
 * No scoring rubric defined yet, and {@code DownloadInstallStepDefinition#scoringRequired} is
 * false, so {@code ExtractionJobServiceImpl} never submits a {@code ScoringJob} for this step —
 * this method is unreachable today. Wire it up to {@code ScoreApiClientProxy} (see {@link
 * RatingScreenshotScorer} for the pattern) once a real rubric exists.
 */
@Component
public class DownloadInstallScreenshotScorer implements ClaimScreenshotScorer {

  @Override
  public ExtractedScoredResult score(
      final Claim claim, final Campaign campaign, final ClaimScreenshot screenshot) {
    return new ExtractedScoredResult(screenshot.getExtractedDetails(), 0);
  }
}
