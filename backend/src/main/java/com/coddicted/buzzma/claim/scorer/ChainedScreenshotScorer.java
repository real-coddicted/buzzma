package com.coddicted.buzzma.claim.scorer;

import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.scoring.entity.ScoringJob;
import java.util.List;

public class ChainedScreenshotScorer implements ClaimScreenshotScorer {
  private final List<ClaimScreenshotScorer> scorers;

  public ChainedScreenshotScorer(final List<ClaimScreenshotScorer> scorers) {
    this.scorers = scorers;
  }

  @Override
  public boolean canScore(final ClaimScreenshot screenshot) {
    return true;
  }

  @Override
  public void score(final ScoringJob job, final ClaimScreenshot screenshot) {
    for (final ClaimScreenshotScorer scorer : this.scorers) {
      if (scorer.canScore(screenshot)) {
        scorer.score(job, screenshot);
        return;
      }
    }
    throw new RuntimeException(
        String.format("Error scoring job:%s, screenshot:%s", job.getId(), screenshot.getId()));
  }
}
