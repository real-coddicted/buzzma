package com.coddicted.buzzma.claim.processor;

import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.extraction.entity.ExtractionJob;

public interface ClaimScreenshotProcessor {
  boolean canProcess(ClaimScreenshot screenshot);

  void process(ExtractionJob job, ClaimScreenshot screenshot);
}
