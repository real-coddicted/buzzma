package com.coddicted.buzzma.extraction.service;

import com.coddicted.buzzma.extraction.entity.ExtractionJob;
import com.coddicted.buzzma.extraction.entity.ExtractionResult;
import java.util.UUID;

public interface ExtractionService {

  ExtractionResult extractSync(
      byte[] imageBytes, String originalFilename, String contentType, UUID requesterId);

  ExtractionJob submitJob(UUID claimScreenshotId, UUID requesterId);

  ExtractionJob processJob(UUID jobId);

  ExtractionJob getById(UUID jobId);
}
