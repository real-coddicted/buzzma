package com.coddicted.buzzma.extraction.scheduler;

import com.coddicted.buzzma.extraction.entity.ExtractionJob;
import com.coddicted.buzzma.extraction.entity.ExtractionJobStatus;
import com.coddicted.buzzma.extraction.persistence.ExtractionJobRepository;
import com.coddicted.buzzma.extraction.service.ExtractionService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ExtractionJobScheduler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExtractionJobScheduler.class);
  private static final int MAX_ATTEMPTS = 3;

  private final ExtractionJobRepository jobRepository;
  private final ExtractionService extractionService;

  public ExtractionJobScheduler(
      final ExtractionJobRepository jobRepository, final ExtractionService extractionService) {
    this.jobRepository = jobRepository;
    this.extractionService = extractionService;
  }

  @Scheduled(fixedDelayString = "${app.extraction.scheduler.fixed-delay-ms:30000}")
  public void processPendingJobs() {
    final List<ExtractionJob> pending =
        jobRepository.findByStatusAndAttemptCountLessThan(
            ExtractionJobStatus.EXTRACTION_JOB_STATUS_PENDING, MAX_ATTEMPTS);
    if (pending.isEmpty()) {
      return;
    }
    LOGGER.debug("ExtractionJobScheduler: processing {} pending job(s)", pending.size());
    for (final ExtractionJob job : pending) {
      try {
        extractionService.processJob(job.getId());
      } catch (Exception e) {
        LOGGER.error(
            "ExtractionJobScheduler: unexpected error processing job {}: {}",
            job.getId(),
            e.getMessage(),
            e);
      }
    }
  }
}
