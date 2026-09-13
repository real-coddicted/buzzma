package com.coddicted.buzzma.campaign.step;

import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import com.coddicted.buzzma.claim.scorer.ClaimScreenshotScorer;
import com.coddicted.buzzma.claim.scorer.RatingScreenshotScorer;
import com.coddicted.buzzma.extraction.service.GeminiExtractionPromptBuilder;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class RatingStepDefinition implements StepDefinition {

  private final GeminiExtractionPromptBuilder promptBuilder;
  private final RatingScreenshotScorer scorer;

  public RatingStepDefinition(
      final GeminiExtractionPromptBuilder promptBuilder, final RatingScreenshotScorer scorer) {
    this.promptBuilder = promptBuilder;
    this.scorer = scorer;
  }

  @Override
  public CampaignStepType stepType() {
    return CampaignStepType.RATING;
  }

  @Override
  public boolean mediaRequired() {
    return true;
  }

  @Override
  public MediaType mediaType() {
    return MediaType.SCREENSHOT;
  }

  @Override
  public Optional<String> extractionPrompt() {
    return Optional.of(this.promptBuilder.buildRatingPrompt());
  }

  @Override
  public boolean scoringRequired() {
    return true;
  }

  @Override
  public List<ScoringCriterion> scoringCriteria() {
    return List.of(new ScoringCriterion("rating", 1.0));
  }

  @Override
  public ClaimScreenshotScorer scorer() {
    return this.scorer;
  }

  /**
   * accountName is also verified against {@code claim.getAccountName()} (see
   * RatingScreenshotScorer).
   */
  @Override
  public List<StepField> fields() {
    return List.of(
        new StepField("platform", false, true),
        new StepField("productName", false, true),
        new StepField("accountName", true, true),
        new StepField("rating", false, true));
  }
}
