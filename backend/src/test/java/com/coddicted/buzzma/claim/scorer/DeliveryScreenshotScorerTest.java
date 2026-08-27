package com.coddicted.buzzma.claim.scorer;

import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_DELIVERY;
import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_ORDER;
import static com.coddicted.buzzma.claim.scorer.Fixtures.JOB_ID;
import static com.coddicted.buzzma.claim.scorer.Fixtures.SCREENSHOT_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.extraction.entity.ScoredValue;
import com.coddicted.buzzma.scoring.entity.ScoringJob;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DeliveryScreenshotScorerTest {

  @Test
  void testCanScoreOnlyMatchesDeliveryScreenshots() {
    final DeliveryScreenshotScorer scorer = new DeliveryScreenshotScorer();

    assertTrue(scorer.canScore(ClaimScreenshot.builder().type(SCREENSHOT_TYPE_DELIVERY).build()));
    assertFalse(scorer.canScore(ClaimScreenshot.builder().type(SCREENSHOT_TYPE_ORDER).build()));
  }

  @Test
  void testScoreIsANoOpThatLeavesTheScreenshotUnscored() {
    final DeliveryScreenshotScorer scorer = new DeliveryScreenshotScorer();
    final Map<String, ScoredValue> details = new HashMap<>();
    details.put("productName", ScoredValue.builder().extractedValue("Test Product").build());
    final ClaimScreenshot screenshot =
        ClaimScreenshot.builder()
            .id(SCREENSHOT_ID)
            .type(SCREENSHOT_TYPE_DELIVERY)
            .extractedDetails(details)
            .build();
    final ScoringJob job = ScoringJob.builder().id(JOB_ID).build();

    scorer.score(job, screenshot);

    assertNull(screenshot.getScore());
    assertEquals(details, screenshot.getExtractedDetails());
  }
}
