package com.coddicted.buzzma.extraction.service.impl;

import com.coddicted.buzzma.claim.service.ClaimScreenshotService;
import com.coddicted.buzzma.extraction.entity.ExtractionJob;
import com.coddicted.buzzma.extraction.entity.ExtractionJobStatus;
import com.coddicted.buzzma.extraction.persistence.ExtractionJobRepository;
import com.coddicted.buzzma.extraction.service.ExtractionJobService;
import com.coddicted.buzzma.scoring.service.ScoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExtractionJobServiceImpl implements ExtractionJobService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExtractionJobServiceImpl.class);
  private static final int MAX_ATTEMPTS = 3;

  private final ExtractionJobRepository jobRepository;
  private final ClaimScreenshotService claimScreenshotService;
  private final ScoringService scoringService;

  public ExtractionJobServiceImpl(
      final ExtractionJobRepository jobRepository,
      final ClaimScreenshotService claimScreenshotService,
      final ScoringService scoringService) {
    this.jobRepository = jobRepository;
    this.claimScreenshotService = claimScreenshotService;
    this.scoringService = scoringService;
  }

  @Override
  @Transactional
  public ExtractionJob processJob(final ExtractionJob job) {
    LOGGER.debug("processJob: starting job {}", job.getId());

    ExtractionJob current =
        job.toBuilder()
            .status(ExtractionJobStatus.EXTRACTION_JOB_STATUS_PROCESSING)
            .attemptCount(job.getAttemptCount() + 1)
            .build();
    current = this.jobRepository.save(current);

    try {
      this.claimScreenshotService.process(current);

      current =
          current.toBuilder()
              .status(ExtractionJobStatus.EXTRACTION_JOB_STATUS_COMPLETED)
              .errorMessage(null)
              .build();
      LOGGER.debug("processJob: completed job {}", current.getId());

      // create the job for next step of scoring
      this.scoringService.submitJob(current.getClaimScreenshotId(), current.getCreatedBy());

    } catch (final RuntimeException e) {
      final boolean exhausted = current.getAttemptCount() >= MAX_ATTEMPTS;
      LOGGER.warn(
          "processJob: extraction failed for job {} (attempt {}/{}): {}",
          current.getId(),
          current.getAttemptCount(),
          MAX_ATTEMPTS,
          e.getMessage());
      current =
          current.toBuilder()
              .status(
                  exhausted
                      ? ExtractionJobStatus.EXTRACTION_JOB_STATUS_FAILED
                      : ExtractionJobStatus.EXTRACTION_JOB_STATUS_PENDING)
              .errorMessage(e.getMessage())
              .build();
    }
    return this.jobRepository.save(current);
  }
}
