package com.coddicted.buzzma.claim.scorer;

import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_ORDER;
import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_RATING;
import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_RETURN;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.scoring.entity.ScoringJob;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ChainedScreenshotScorerTest {

  /** Records whether it was invoked, without any Mockito matcher involved. */
  private static final class RecordingScorer implements ClaimScreenshotScorer {
    private final boolean canScore;
    private boolean scored;

    RecordingScorer(final boolean canScore) {
      this.canScore = canScore;
    }

    @Override
    public boolean canScore(final ClaimScreenshot screenshot) {
      return this.canScore;
    }

    @Override
    public void score(final ScoringJob job, final ClaimScreenshot screenshot) {
      this.scored = true;
    }
  }

  @Test
  void testScoreDelegatesToTheFirstScorerThatCanScore() {
    final RecordingScorer orderScorer = new RecordingScorer(false);
    final RecordingScorer ratingScorer = new RecordingScorer(true);
    final RecordingScorer returnScorer = new RecordingScorer(true);
    final ChainedScreenshotScorer chain =
        new ChainedScreenshotScorer(List.of(orderScorer, ratingScorer, returnScorer));

    chain.score(
        ScoringJob.builder().build(),
        ClaimScreenshot.builder().type(SCREENSHOT_TYPE_RATING).build());

    assertFalse(orderScorer.scored);
    assertTrue(ratingScorer.scored);
    assertFalse(returnScorer.scored);
  }

  @Test
  void testScoreThrowsWhenNoScorerCanScore() {
    final RecordingScorer orderScorer = new RecordingScorer(false);
    final RecordingScorer returnScorer = new RecordingScorer(false);
    final ChainedScreenshotScorer chain =
        new ChainedScreenshotScorer(List.of(orderScorer, returnScorer));
    final ScoringJob job =
        ScoringJob.builder().id(UUID.fromString("33333333-3333-3333-3333-333333333333")).build();
    final ClaimScreenshot screenshot =
        ClaimScreenshot.builder()
            .id(UUID.fromString("44444444-4444-4444-4444-444444444444"))
            .type(SCREENSHOT_TYPE_ORDER)
            .build();

    assertThrows(RuntimeException.class, () -> chain.score(job, screenshot));

    assertFalse(orderScorer.scored);
    assertFalse(returnScorer.scored);
  }

  @Test
  void testCanScoreAlwaysReturnsTrue() {
    final ChainedScreenshotScorer chain = new ChainedScreenshotScorer(List.of());

    assertTrue(chain.canScore(ClaimScreenshot.builder().type(SCREENSHOT_TYPE_RETURN).build()));
  }
}
