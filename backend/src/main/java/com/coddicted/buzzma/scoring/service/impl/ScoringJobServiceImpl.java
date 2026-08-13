package com.coddicted.buzzma.scoring.service.impl;

import com.coddicted.buzzma.claim.service.ClaimScreenshotService;
import com.coddicted.buzzma.scoring.entity.ScoringJob;
import com.coddicted.buzzma.scoring.entity.ScoringJobStatus;
import com.coddicted.buzzma.scoring.persistence.ScoringJobRepository;
import com.coddicted.buzzma.scoring.service.ScoringJobService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScoringJobServiceImpl implements ScoringJobService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ScoringJobServiceImpl.class);
  private static final int MAX_ATTEMPTS = 3;

  @PersistenceContext private EntityManager entityManager;

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
  @SuppressWarnings("unchecked")
  public List<UUID> claimBatchForProcessing(final int batchSize, final int maxAttempts) {
    final List<Object> rawIds =
        this.entityManager
            .createNativeQuery(
                """
            UPDATE scoring_jobs
            SET status             = 'SCORING_JOB_STATUS_PROCESSING',
                attempt_count      = attempt_count + 1,
                last_attempted_at  = NOW()
            WHERE id IN (
                SELECT id FROM scoring_jobs
                WHERE status = 'SCORING_JOB_STATUS_PENDING'
                  AND attempt_count < :maxAttempts
                  AND is_deleted = false
                ORDER BY created_at
                LIMIT :batchSize
                FOR UPDATE SKIP LOCKED
            )
            RETURNING id
            """)
            .setParameter("maxAttempts", maxAttempts)
            .setParameter("batchSize", batchSize)
            .getResultList();

    return rawIds.stream()
        .map(id -> id instanceof UUID ? (UUID) id : UUID.fromString(id.toString()))
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public ScoringJob processJob(final ScoringJob job) {
    LOGGER.debug("processJob: starting job {}", job.getId());

    ScoringJob current = job;
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
