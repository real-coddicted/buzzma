package com.coddicted.buzzma.claim.step;

import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import com.coddicted.buzzma.claim.scorer.ClaimScreenshotScorer;
import com.coddicted.buzzma.claim.scorer.ReviewScreenshotScorer;
import com.coddicted.buzzma.extraction.service.GeminiExtractionPromptBuilder;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ReviewStepDefinition implements StepDefinition {

  private final GeminiExtractionPromptBuilder promptBuilder;
  private final ReviewScreenshotScorer scorer;

  public ReviewStepDefinition(
      final GeminiExtractionPromptBuilder promptBuilder, final ReviewScreenshotScorer scorer) {
    this.promptBuilder = promptBuilder;
    this.scorer = scorer;
  }

  @Override
  public CampaignStepType stepType() {
    return CampaignStepType.REVIEW;
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
    return Optional.of(this.promptBuilder.buildReviewPrompt());
  }

  @Override
  public boolean scoringRequired() {
    return true;
  }

  @Override
  public List<ScoringCriterion> scoringCriteria() {
    return List.of(new ScoringCriterion("reviewText", 1.0));
  }

  @Override
  public ClaimScreenshotScorer scorer() {
    return this.scorer;
  }

  /**
   * reviewUrl and accountName are supplied by the buyer (see {@code ClaimController#submitReview},
   * {@code claim.getAccountName()}) and also extracted from the screenshot, so
   * ReviewScreenshotScorer can verify the buyer's claim against it.
   */
  @Override
  public List<StepField> fields() {
    return List.of(
        new StepField("reviewUrl", true, true),
        new StepField("platform", false, true),
        new StepField("productName", false, true),
        new StepField("reviewText", false, true),
        new StepField("accountName", true, true),
        new StepField("reviewDate", false, true));
  }
}
