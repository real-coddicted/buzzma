package com.coddicted.buzzma.extraction.scheduler;

import com.coddicted.buzzma.extraction.entity.ExtractionJob;
import com.coddicted.buzzma.extraction.entity.ExtractionJobStatus;
import com.coddicted.buzzma.extraction.persistence.ExtractionJobRepository;
import com.coddicted.buzzma.extraction.service.ExtractionJobService;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ExtractionJobScheduler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExtractionJobScheduler.class);
  private static final int MAX_ATTEMPTS = 3;

  private final ExtractionJobRepository jobRepository;
  private final ExtractionJobService extractionJobService;
  private final Executor taskExecutor;

  @Value("${app.extraction.scheduler.batch-size:5}")
  private int batchSize;

  public ExtractionJobScheduler(
      final ExtractionJobRepository jobRepository,
      final ExtractionJobService extractionJobService,
      @Qualifier("extractionTaskExecutor") final Executor taskExecutor) {
    this.jobRepository = jobRepository;
    this.extractionJobService = extractionJobService;
    this.taskExecutor = taskExecutor;
  }

  @Scheduled(fixedDelayString = "${app.extraction.scheduler.fixed-delay-ms:30000}")
  public void processPendingJobs() {
    final List<ExtractionJob> pending =
        jobRepository.findByStatusAndAttemptCountLessThan(
            ExtractionJobStatus.EXTRACTION_JOB_STATUS_PENDING,
            MAX_ATTEMPTS,
            PageRequest.of(0, batchSize));
    if (pending.isEmpty()) {
      return;
    }
    LOGGER.info("ExtractionJobScheduler: processing {} pending job(s)", pending.size());

    final List<CompletableFuture<Void>> futures =
        pending.stream()
            .map(
                job ->
                    CompletableFuture.runAsync(
                        () -> {
                          try {
                            extractionJobService.processJob(job);
                          } catch (final Exception e) {
                            LOGGER.error(
                                "ExtractionJobScheduler: unexpected error processing job {}: {}",
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
