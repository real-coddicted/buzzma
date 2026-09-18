package com.coddicted.buzzma.claim.scorer;

import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_DOWNLOAD_INSTALL;

import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.persistence.ClaimScreenshotRepository;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.scoring.entity.ScoringJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * No-op for now: leaves the screenshot's score null (pending manual review) instead of calling the
 * scoring service, since Download & Install has no verification rubric or scoring dataset defined
 * yet. Still updates the claim's aggregate score so the pipeline isn't left in a half-updated
 * state. Wire this up to {@code ScoreApiClientProxy} (see {@link RatingScreenshotScorer} for the
 * pattern) once a real rubric exists.
 */
@Component
public class DownloadInstallScreenshotScorer implements ClaimScreenshotScorer {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(DownloadInstallScreenshotScorer.class);

  private final ClaimScreenshotRepository screenshotRepository;
  private final ClaimService claimService;

  public DownloadInstallScreenshotScorer(
      final ClaimScreenshotRepository screenshotRepository, final ClaimService claimService) {
    this.screenshotRepository = screenshotRepository;
    this.claimService = claimService;
  }

  @Override
  public boolean canScore(final ClaimScreenshot screenshot) {
    return SCREENSHOT_TYPE_DOWNLOAD_INSTALL == screenshot.getType();
  }

  @Override
  public void score(final ScoringJob job, final ClaimScreenshot screenshot) {
    LOGGER.info(
        "scoreDownloadInstallScreenshot: no scoring rubric yet, leaving unscored for job {},"
            + " screenshot {}",
        job.getId(),
        screenshot.getId());

    this.screenshotRepository.save(screenshot);
    this.claimService.updateClaimScore(screenshot.getClaimId());
  }
}
