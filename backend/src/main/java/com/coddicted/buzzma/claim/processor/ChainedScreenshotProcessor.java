package com.coddicted.buzzma.claim.processor;

import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.extraction.entity.ExtractionJob;
import java.util.List;

public class ChainedScreenshotProcessor implements ClaimScreenshotProcessor {
  private final List<ClaimScreenshotProcessor> processors;

  public ChainedScreenshotProcessor(final List<ClaimScreenshotProcessor> processors) {
    this.processors = processors;
  }

  @Override
  public boolean canProcess(final ClaimScreenshot screenshot) {
    return true;
  }

  @Override
  public void process(final ExtractionJob job, final ClaimScreenshot screenshot) {
    for (final ClaimScreenshotProcessor processor : this.processors) {
      if (processor.canProcess(screenshot)) {
        processor.process(job, screenshot);
        return;
      }
    }
    throw new RuntimeException(
        String.format("Error processing job:%s, screenshot:%s", job.getId(), screenshot.getId()));
  }
}
