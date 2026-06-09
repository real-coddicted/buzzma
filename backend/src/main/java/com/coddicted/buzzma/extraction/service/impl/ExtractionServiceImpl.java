package com.coddicted.buzzma.extraction.service.impl;

import com.coddicted.buzzma.claim.service.ClaimScreenshotService;
import com.coddicted.buzzma.extraction.entity.ExtractionJob;
import com.coddicted.buzzma.extraction.entity.ExtractionJobStatus;
import com.coddicted.buzzma.extraction.persistence.ExtractionJobRepository;
import com.coddicted.buzzma.extraction.service.ExtractionService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import com.coddicted.buzzma.shared.gemini.GeminiException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExtractionServiceImpl extends BaseCrudService implements ExtractionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExtractionServiceImpl.class);
  private static final int MAX_ATTEMPTS = 3;

  private final ExtractionJobRepository jobRepository;
  private final ClaimScreenshotService claimScreenshotService;

  public ExtractionServiceImpl(
      final ExtractionJobRepository jobRepository,
      final ClaimScreenshotService claimScreenshotService) {
    this.jobRepository = jobRepository;
    this.claimScreenshotService = claimScreenshotService;
  }

  @Override
  @Transactional
  public ExtractionJob submitJob(final UUID claimScreenshotId, final UUID requesterId) {
    LOGGER.debug("submitJob: creating job for claimScreenshot {}", claimScreenshotId);
    final ExtractionJob job =
        ExtractionJob.builder()
            .claimScreenshotId(claimScreenshotId)
            .status(ExtractionJobStatus.EXTRACTION_JOB_STATUS_PENDING)
            .attemptCount(0)
            .isDeleted(false)
            .createdBy(requesterId)
            .updatedBy(requesterId)
            .build();
    final ExtractionJob saved = this.jobRepository.save(job);
    LOGGER.debug(
        "submitJob: created job {} for claimScreenshot {}", saved.getId(), claimScreenshotId);
    return saved;
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
    } catch (final GeminiException e) {
      final boolean exhausted = current.getAttemptCount() >= MAX_ATTEMPTS;
      LOGGER.warn(
          "processJob: Gemini failed for job {} (attempt {}/{}): {}",
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

  @Override
  @Transactional(readOnly = true)
  public ExtractionJob getById(final UUID jobId) {
    return this.jobRepository
        .findByIdAndIsDeletedFalse(jobId)
        .orElseThrow(() -> new NotFoundException("ExtractionJob not found: " + jobId));
  }
}
