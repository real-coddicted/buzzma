package com.coddicted.buzzma.extraction.service;

import com.coddicted.buzzma.extraction.entity.ExtractionJob;
import java.util.List;
import java.util.UUID;

public interface ExtractionService {

  ExtractionJob extractSync(
      byte[] imageBytes, String originalFilename, String contentType, UUID requesterId);

  ExtractionJob submitJob(
      byte[] imageBytes, String originalFilename, String contentType, UUID requesterId);

  ExtractionJob processJob(UUID jobId);

  ExtractionJob getById(UUID jobId, UUID requesterId);

  List<ExtractionJob> listByUser(UUID requesterId);
}
