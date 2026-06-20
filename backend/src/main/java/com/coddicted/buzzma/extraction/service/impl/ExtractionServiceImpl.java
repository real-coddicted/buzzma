package com.coddicted.buzzma.extraction.service.impl;

import com.coddicted.buzzma.extraction.entity.ExtractionJob;
import com.coddicted.buzzma.extraction.entity.ExtractionJobStatus;
import com.coddicted.buzzma.extraction.persistence.ExtractionJobRepository;
import com.coddicted.buzzma.extraction.service.ExtractionService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExtractionServiceImpl extends BaseCrudService implements ExtractionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExtractionServiceImpl.class);

  private final ExtractionJobRepository jobRepository;

  public ExtractionServiceImpl(final ExtractionJobRepository jobRepository) {
    this.jobRepository = jobRepository;
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
  @Transactional(readOnly = true)
  public ExtractionJob getById(final UUID jobId) {
    return this.jobRepository
        .findByIdAndIsDeletedFalse(jobId)
        .orElseThrow(() -> new NotFoundException("ExtractionJob not found: " + jobId));
  }
}
