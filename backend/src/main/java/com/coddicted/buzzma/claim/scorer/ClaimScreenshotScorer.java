package com.coddicted.buzzma.claim.scorer;

import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.scoring.entity.ScoringJob;

public interface ClaimScreenshotScorer {
  boolean canScore(ClaimScreenshot screenshot);

  void score(ScoringJob job, ClaimScreenshot screenshot);
}
