package com.coddicted.buzzma.claim.step;

import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import com.coddicted.buzzma.claim.scorer.ClaimScreenshotScorer;
import com.coddicted.buzzma.claim.scorer.SellerFeedbackScreenshotScorer;
import com.coddicted.buzzma.extraction.service.GeminiExtractionPromptBuilder;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class SellerFeedbackStepDefinition implements StepDefinition {

  private final GeminiExtractionPromptBuilder promptBuilder;
  private final SellerFeedbackScreenshotScorer scorer;

  public SellerFeedbackStepDefinition(
      final GeminiExtractionPromptBuilder promptBuilder,
      final SellerFeedbackScreenshotScorer scorer) {
    this.promptBuilder = promptBuilder;
    this.scorer = scorer;
  }

  @Override
  public CampaignStepType stepType() {
    return CampaignStepType.SELLER_FEEDBACK;
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
    return Optional.of(this.promptBuilder.buildSellerFeedbackPrompt());
  }

  @Override
  public boolean scoringRequired() {
    return true;
  }

  @Override
  public ClaimScreenshotScorer scorer() {
    return this.scorer;
  }

  /**
   * sellerName is also verified against {@code campaign.getSellerName()} (see
   * SellerFeedbackScreenshotScorer) — that's campaign config, not buyer input, so it's not
   * userProvided.
   */
  @Override
  public List<StepField> fields() {
    return List.of(
        new StepField("platform", false, true),
        new StepField("sellerName", false, true),
        new StepField("productName", false, true),
        new StepField("orderId", false, true),
        new StepField("rating", false, true),
        new StepField("feedbackText", false, true),
        new StepField("comment", false, true),
        new StepField("reviewerName", false, true));
  }
}
