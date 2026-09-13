package com.coddicted.buzzma.claim.step;

import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import com.coddicted.buzzma.claim.scorer.ClaimScreenshotScorer;
import com.coddicted.buzzma.claim.scorer.DeliveryScreenshotScorer;
import com.coddicted.buzzma.extraction.service.GeminiExtractionPromptBuilder;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class DeliveryStepDefinition implements StepDefinition {

  private final GeminiExtractionPromptBuilder promptBuilder;
  private final DeliveryScreenshotScorer scorer;

  public DeliveryStepDefinition(
      final GeminiExtractionPromptBuilder promptBuilder, final DeliveryScreenshotScorer scorer) {
    this.promptBuilder = promptBuilder;
    this.scorer = scorer;
  }

  @Override
  public CampaignStepType stepType() {
    return CampaignStepType.DELIVERY;
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
    return Optional.of(this.promptBuilder.buildDeliveryPrompt());
  }

  @Override
  public boolean scoringRequired() {
    return true;
  }

  @Override
  public List<ScoringCriterion> scoringCriteria() {
    return List.of(new ScoringCriterion("deliveryStatus", 1.0));
  }

  @Override
  public ClaimScreenshotScorer scorer() {
    return this.scorer;
  }

  @Override
  public List<StepField> fields() {
    return List.of(
        new StepField("platform", false, true),
        new StepField("productName", false, true),
        new StepField("orderId", false, true),
        new StepField("deliveryDate", false, true),
        new StepField("deliveryStatus", false, true),
        new StepField("orderedBy", false, true));
  }
}
