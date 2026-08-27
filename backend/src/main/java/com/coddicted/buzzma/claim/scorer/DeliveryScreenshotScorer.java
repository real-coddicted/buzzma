package com.coddicted.buzzma.claim.scorer;

import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_DELIVERY;

import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.scoring.entity.ScoringJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Delivery screenshots have no campaign/claim data to reconcile against, so there is nothing
 * meaningful to auto-score; the extracted details are left for manual reviewer verification.
 */
@Component
public class DeliveryScreenshotScorer implements ClaimScreenshotScorer {

  private static final Logger LOGGER = LoggerFactory.getLogger(DeliveryScreenshotScorer.class);

  @Override
  public boolean canScore(final ClaimScreenshot screenshot) {
    return SCREENSHOT_TYPE_DELIVERY == screenshot.getType();
  }

  @Override
  public void score(final ScoringJob job, final ClaimScreenshot screenshot) {
    LOGGER.info(
        "scoreDeliveryScreenshot: no-op, job {}, screenshot {}", job.getId(), screenshot.getId());
  }
}
