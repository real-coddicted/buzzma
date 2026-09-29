package com.coddicted.buzzma.claim.step;

import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import com.coddicted.buzzma.claim.scorer.ClaimScreenshotScorer;
import com.coddicted.buzzma.claim.scorer.LikeScreenshotScorer;
import com.coddicted.buzzma.extraction.service.GeminiExtractionPromptBuilder;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class LikeStepDefinition implements StepDefinition {

  private final GeminiExtractionPromptBuilder promptBuilder;
  private final LikeScreenshotScorer scorer;

  public LikeStepDefinition(
      final GeminiExtractionPromptBuilder promptBuilder, final LikeScreenshotScorer scorer) {
    this.promptBuilder = promptBuilder;
    this.scorer = scorer;
  }

  @Override
  public CampaignStepType stepType() {
    return CampaignStepType.LIKE;
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
    return Optional.of(this.promptBuilder.buildLikePrompt());
  }

  @Override
  public boolean scoringRequired() {
    return true;
  }

  @Override
  public ClaimScreenshotScorer scorer() {
    return this.scorer;
  }

  @Override
  public List<StepField> fields() {
    return List.of(
        new StepField("platform", false, true),
        new StepField("accountName", true, true),
        new StepField("contentTitle", false, true),
        new StepField("likeStatus", false, true));
  }
}
