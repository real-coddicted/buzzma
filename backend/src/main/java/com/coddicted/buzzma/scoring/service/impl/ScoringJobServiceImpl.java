package com.coddicted.buzzma.scoring.service.impl;

import com.coddicted.buzzma.claim.service.ClaimScreenshotService;
import com.coddicted.buzzma.scoring.entity.ScoringJob;
import com.coddicted.buzzma.scoring.entity.ScoringJobStatus;
import com.coddicted.buzzma.scoring.persistence.ScoringJobRepository;
import com.coddicted.buzzma.scoring.service.ScoringJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScoringJobServiceImpl implements ScoringJobService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ScoringJobServiceImpl.class);
  private static final int MAX_ATTEMPTS = 3;

  private final ScoringJobRepository jobRepository;
  private final ClaimScreenshotService claimScreenshotService;

  public ScoringJobServiceImpl(
      final ScoringJobRepository jobRepository,
      final ClaimScreenshotService claimScreenshotService) {
    this.jobRepository = jobRepository;
    this.claimScreenshotService = claimScreenshotService;
  }

  @Override
  @Transactional
  public ScoringJob processJob(final ScoringJob job) {
    LOGGER.debug("processJob: starting job {}", job.getId());

    ScoringJob current =
        job.toBuilder()
            .status(ScoringJobStatus.SCORING_JOB_STATUS_PROCESSING)
            .attemptCount(job.getAttemptCount() + 1)
            .build();
    current = this.jobRepository.save(current);

    try {
      this.claimScreenshotService.processScoring(current);

      current =
          current.toBuilder()
              .status(ScoringJobStatus.SCORING_JOB_STATUS_COMPLETED)
              .errorMessage(null)
              .build();
      LOGGER.debug("processJob: completed job {}", current.getId());
    } catch (final RuntimeException e) {
      final boolean exhausted = current.getAttemptCount() >= MAX_ATTEMPTS;
      LOGGER.warn(
          "processJob: scoring failed for job {} (attempt {}/{}): {}",
          current.getId(),
          current.getAttemptCount(),
          MAX_ATTEMPTS,
          e.getMessage());
      current =
          current.toBuilder()
              .status(
                  exhausted
                      ? ScoringJobStatus.SCORING_JOB_STATUS_FAILED
                      : ScoringJobStatus.SCORING_JOB_STATUS_PENDING)
              .errorMessage(e.getMessage())
              .build();
    }
    return this.jobRepository.save(current);
  }
}
