package com.coddicted.buzzma.extraction.service;

import com.coddicted.buzzma.extraction.entity.ExtractionJob;
import java.util.UUID;

public interface ExtractionService {

  ExtractionJob submitJob(UUID claimScreenshotId, UUID requesterId);

  ExtractionJob getById(UUID jobId);
}
