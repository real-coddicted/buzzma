package com.coddicted.buzzma.claim.step;

import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import com.coddicted.buzzma.claim.entity.ScreenshotType;
import com.coddicted.buzzma.claim.scorer.ClaimScreenshotScorer;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Declares the shape of a single campaign step: what media it collects, whether/how AI extraction
 * runs, and how the extracted result is scored. One implementation per {@link CampaignStepType},
 * registered in {@link StepDefinitionRegistry}.
 */
public interface StepDefinition {

  CampaignStepType stepType();

  /**
   * The {@link ScreenshotType} a {@code ClaimScreenshot} carries for this step, derived from {@link
   * #stepType()}. Bridges the campaign-step and screenshot-media enums until they're unified; empty
   * for steps with no media (e.g. cashback).
   */
  default Optional<ScreenshotType> screenshotType() {
    return switch (stepType()) {
      case ORDER -> Optional.of(ScreenshotType.SCREENSHOT_TYPE_ORDER);
      case RATING -> Optional.of(ScreenshotType.SCREENSHOT_TYPE_RATING);
      case REVIEW -> Optional.of(ScreenshotType.SCREENSHOT_TYPE_REVIEW);
      case RETURN_WINDOW -> Optional.of(ScreenshotType.SCREENSHOT_TYPE_RETURN);
      case DELIVERY -> Optional.of(ScreenshotType.SCREENSHOT_TYPE_DELIVERY);
      case SELLER_FEEDBACK -> Optional.of(ScreenshotType.SCREENSHOT_TYPE_SELLER_FEEDBACK);
      case DOWNLOAD_INSTALL -> Optional.of(ScreenshotType.SCREENSHOT_TYPE_DOWNLOAD_INSTALL);
      case CASHBACK -> Optional.empty();
    };
  }

  boolean mediaRequired();

  /** Only meaningful when {@link #mediaRequired()} is true. */
  MediaType mediaType();

  /**
   * The data points this step collects, and whether each comes from the buyer or from AI
   * extraction.
   */
  List<StepField> fields();

  /**
   * Empty when this step doesn't run AI extraction. Whether extraction runs sync or async is
   * decided by the caller (a request vs. the batch scheduler), not by the step.
   */
  Optional<String> extractionPrompt();

  boolean scoringRequired();

  /** Empty when {@link #scoringRequired()} is false. */
  List<ScoringCriterion> scoringCriteria();

  /**
   * This step's scorer. Dispatch goes through here (one lookup, no chain-of-responsibility scan) —
   * the score-composition logic itself stays in the concrete {@link ClaimScreenshotScorer} since it
   * genuinely differs per step (e.g. Order folds a local score into the overall min; Rating's
   * manual rating cutoff does not feed into the overall score at all).
   */
  ClaimScreenshotScorer scorer();

  /** Weighted average of {@code criterionScores} over {@link #scoringCriteria()}. */
  default double calculateOverallScore(final Map<String, Double> criterionScores) {
    if (!scoringRequired() || scoringCriteria().isEmpty()) {
      return 0;
    }
    double weightedSum = 0;
    double totalWeight = 0;
    for (final ScoringCriterion criterion : scoringCriteria()) {
      final Double score = criterionScores.get(criterion.name());
      if (score == null) {
        continue;
      }
      weightedSum += score * criterion.weight();
      totalWeight += criterion.weight();
    }
    return totalWeight == 0 ? 0 : weightedSum / totalWeight;
  }
}
