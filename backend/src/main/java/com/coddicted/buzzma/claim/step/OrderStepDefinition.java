package com.coddicted.buzzma.claim.step;

import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import com.coddicted.buzzma.claim.scorer.ClaimScreenshotScorer;
import com.coddicted.buzzma.claim.scorer.OrderScreenshotScorer;
import com.coddicted.buzzma.extraction.service.GeminiExtractionPromptBuilder;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Order is the campaign's blocking first step, so extraction runs synchronously. */
@Component
public class OrderStepDefinition implements StepDefinition {

  private final GeminiExtractionPromptBuilder promptBuilder;
  private final OrderScreenshotScorer scorer;

  public OrderStepDefinition(
      final GeminiExtractionPromptBuilder promptBuilder, final OrderScreenshotScorer scorer) {
    this.promptBuilder = promptBuilder;
    this.scorer = scorer;
  }

  @Override
  public CampaignStepType stepType() {
    return CampaignStepType.ORDER;
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
    return Optional.of(this.promptBuilder.build());
  }

  @Override
  public boolean scoringRequired() {
    return false;
  }

  @Override
  public List<ScoringCriterion> scoringCriteria() {
    return List.of();
  }

  @Override
  public ClaimScreenshotScorer scorer() {
    return this.scorer;
  }

  /**
   * The buyer runs {@code /extraction/sync} to pre-fill these from the screenshot (aiExtracted),
   * then confirms or edits them before the actual claim-creation submission (userProvided) — both
   * contribute to the final value.
   */
  @Override
  public List<StepField> fields() {
    return List.of(
        new StepField("platform", true, true),
        new StepField("orderId", true, true),
        new StepField("orderDate", true, true),
        new StepField("productName", true, true),
        new StepField("sellerName", true, true),
        new StepField("amount", true, true),
        new StepField("orderedBy", true, true));
  }
}
