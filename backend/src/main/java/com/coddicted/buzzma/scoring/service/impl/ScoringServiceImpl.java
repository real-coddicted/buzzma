package com.coddicted.buzzma.scoring.service.impl;

import com.coddicted.buzzma.scoring.entity.ScoringJob;
import com.coddicted.buzzma.scoring.entity.ScoringJobStatus;
import com.coddicted.buzzma.scoring.persistence.ScoringJobRepository;
import com.coddicted.buzzma.scoring.service.ScoringService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScoringServiceImpl extends BaseCrudService implements ScoringService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ScoringServiceImpl.class);

  private final ScoringJobRepository jobRepository;

  public ScoringServiceImpl(final ScoringJobRepository jobRepository) {
    this.jobRepository = jobRepository;
  }

  @Override
  @Transactional
  public ScoringJob submitJob(final UUID claimScreenshotId, final UUID requesterId) {
    LOGGER.debug("submitJob: creating job for claimScreenshot {}", claimScreenshotId);
    final ScoringJob job =
        ScoringJob.builder()
            .claimScreenshotId(claimScreenshotId)
            .status(ScoringJobStatus.SCORING_JOB_STATUS_PENDING)
            .attemptCount(0)
            .isDeleted(false)
            .createdBy(requesterId)
            .updatedBy(requesterId)
            .build();
    final ScoringJob saved = this.jobRepository.save(job);
    LOGGER.debug(
        "submitJob: created job {} for claimScreenshot {}", saved.getId(), claimScreenshotId);
    return saved;
  }

  @Override
  @Transactional(readOnly = true)
  public ScoringJob getById(final UUID jobId) {
    return this.jobRepository
        .findByIdAndIsDeletedFalse(jobId)
        .orElseThrow(() -> new NotFoundException("ScoringJob not found: " + jobId));
  }
}
