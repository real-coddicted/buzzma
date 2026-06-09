package com.coddicted.buzzma.claim.service;

import com.coddicted.buzzma.extraction.entity.ExtractionJob;
import com.coddicted.buzzma.extraction.entity.ExtractionResult;
import java.util.UUID;

public interface ClaimScreenshotService {

  ExtractionResult extractSync(
      byte[] imageBytes, String originalFilename, String contentType, UUID requesterId);

  void process(ExtractionJob job);
}
