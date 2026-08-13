package com.coddicted.buzzma.scoring.scheduler;

import com.coddicted.buzzma.scoring.entity.ScoringJob;
import com.coddicted.buzzma.scoring.persistence.ScoringJobRepository;
import com.coddicted.buzzma.scoring.service.ScoringJobService;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScoringJobScheduler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ScoringJobScheduler.class);
  private static final int MAX_ATTEMPTS = 3;

  private final ScoringJobRepository jobRepository;
  private final ScoringJobService scoringJobService;
  private final Executor taskExecutor;

  @Value("${app.scoring.scheduler.batch-size:5}")
  private int batchSize;

  @Value("${app.scoring.scheduler.stale-threshold-minutes:30}")
  private int staleThresholdMinutes;

  public ScoringJobScheduler(
      final ScoringJobRepository jobRepository,
      final ScoringJobService scoringJobService,
      @Qualifier("scoringTaskExecutor") final Executor taskExecutor) {
    this.jobRepository = jobRepository;
    this.scoringJobService = scoringJobService;
    this.taskExecutor = taskExecutor;
  }

  @Scheduled(fixedDelayString = "${app.scoring.scheduler.fixed-delay-ms:30000}")
  public void processPendingJobs() {
    final int reset = jobRepository.resetStaleInProgressJobs(staleThresholdMinutes);
    if (reset > 0) {
      LOGGER.warn(
          "ScoringJobScheduler: reset {} stale PROCESSING job(s) (>{} min) to PENDING",
          reset,
          staleThresholdMinutes);
    }

    final List<UUID> claimedIds =
        scoringJobService.claimBatchForProcessing(batchSize, MAX_ATTEMPTS);
    if (claimedIds.isEmpty()) {
      return;
    }

    final List<ScoringJob> pending = jobRepository.findAllById(claimedIds);
    LOGGER.info("ScoringJobScheduler: processing {} pending job(s)", pending.size());

    final List<CompletableFuture<Void>> futures =
        pending.stream()
            .map(
                job ->
                    CompletableFuture.runAsync(
                        () -> {
                          try {
                            scoringJobService.processJob(job);
                          } catch (final Exception e) {
                            LOGGER.error(
                                "ScoringJobScheduler: unexpected error processing job {}: {}",
                                job.getId(),
                                e.getMessage(),
                                e);
                          }
                        },
                        taskExecutor))
            .toList();
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
  }
}
