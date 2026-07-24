package com.coddicted.buzzma.claim.processor;

import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_ORDER;
import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_RATING;
import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_RETURN;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.extraction.entity.ExtractionJob;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ChainedScreenshotProcessorTest {

  /** Records whether it was invoked, without any Mockito matcher involved. */
  private static final class RecordingProcessor implements ClaimScreenshotProcessor {
    private final boolean canProcess;
    private boolean processed;

    RecordingProcessor(final boolean canProcess) {
      this.canProcess = canProcess;
    }

    @Override
    public boolean canProcess(final ClaimScreenshot screenshot) {
      return this.canProcess;
    }

    @Override
    public void process(final ExtractionJob job, final ClaimScreenshot screenshot) {
      this.processed = true;
    }
  }

  @Test
  void testProcessDelegatesToTheFirstProcessorThatCanProcess() {
    final RecordingProcessor orderProcessor = new RecordingProcessor(false);
    final RecordingProcessor ratingProcessor = new RecordingProcessor(true);
    final RecordingProcessor returnProcessor = new RecordingProcessor(true);
    final ChainedScreenshotProcessor chain =
        new ChainedScreenshotProcessor(List.of(orderProcessor, ratingProcessor, returnProcessor));

    chain.process(
        ExtractionJob.builder().build(),
        ClaimScreenshot.builder().type(SCREENSHOT_TYPE_RATING).build());

    assertFalse(orderProcessor.processed);
    assertTrue(ratingProcessor.processed);
    assertFalse(returnProcessor.processed);
  }

  @Test
  void testProcessThrowsWhenNoProcessorCanProcess() {
    final RecordingProcessor orderProcessor = new RecordingProcessor(false);
    final RecordingProcessor returnProcessor = new RecordingProcessor(false);
    final ChainedScreenshotProcessor chain =
        new ChainedScreenshotProcessor(List.of(orderProcessor, returnProcessor));
    final ExtractionJob job =
        ExtractionJob.builder().id(UUID.fromString("33333333-3333-3333-3333-333333333333")).build();
    final ClaimScreenshot screenshot =
        ClaimScreenshot.builder()
            .id(UUID.fromString("44444444-4444-4444-4444-444444444444"))
            .type(SCREENSHOT_TYPE_ORDER)
            .build();

    assertThrows(RuntimeException.class, () -> chain.process(job, screenshot));

    assertFalse(orderProcessor.processed);
    assertFalse(returnProcessor.processed);
  }

  @Test
  void testCanProcessAlwaysReturnsTrue() {
    final ChainedScreenshotProcessor chain = new ChainedScreenshotProcessor(List.of());

    assertTrue(chain.canProcess(ClaimScreenshot.builder().type(SCREENSHOT_TYPE_RETURN).build()));
  }
}
