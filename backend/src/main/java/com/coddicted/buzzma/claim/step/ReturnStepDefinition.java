package com.coddicted.buzzma.claim.step;

import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import com.coddicted.buzzma.claim.scorer.ClaimScreenshotScorer;
import com.coddicted.buzzma.claim.scorer.ReturnScreenshotScorer;
import com.coddicted.buzzma.extraction.service.GeminiExtractionPromptBuilder;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ReturnStepDefinition implements StepDefinition {

  private final GeminiExtractionPromptBuilder promptBuilder;
  private final ReturnScreenshotScorer scorer;

  public ReturnStepDefinition(
      final GeminiExtractionPromptBuilder promptBuilder, final ReturnScreenshotScorer scorer) {
    this.promptBuilder = promptBuilder;
    this.scorer = scorer;
  }

  @Override
  public CampaignStepType stepType() {
    return CampaignStepType.RETURN_WINDOW;
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
    return Optional.of(this.promptBuilder.buildReturnPrompt());
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
   * accountName is also verified against {@code claim.getAccountName()} (see
   * ReturnScreenshotScorer).
   */
  @Override
  public List<StepField> fields() {
    return List.of(
        new StepField("platform", false, true),
        new StepField("productName", false, true),
        new StepField("accountName", true, true),
        new StepField("returnWindowClosedText", false, true),
        new StepField("returnWindowClosedDate", false, true));
  }
}
