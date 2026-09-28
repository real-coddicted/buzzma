package com.coddicted.buzzma.claim.step;

import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import com.coddicted.buzzma.claim.scorer.ClaimScreenshotScorer;
import com.coddicted.buzzma.claim.scorer.LikeScreenshotScorer;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class LikeStepDefinition implements StepDefinition {

  private final LikeScreenshotScorer scorer;

  public LikeStepDefinition(final LikeScreenshotScorer scorer) {
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
    return Optional.empty();
  }

  @Override
  public boolean scoringRequired() {
    return false;
  }

  @Override
  public ClaimScreenshotScorer scorer() {
    return this.scorer;
  }

  @Override
  public List<StepField> fields() {
    return List.of();
  }
}
