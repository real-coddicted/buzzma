package com.coddicted.buzzma.claim.step;

import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import com.coddicted.buzzma.claim.scorer.ClaimScreenshotScorer;
import com.coddicted.buzzma.claim.scorer.ViewScreenshotScorer;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ViewStepDefinition implements StepDefinition {

  private final ViewScreenshotScorer scorer;

  public ViewStepDefinition(final ViewScreenshotScorer scorer) {
    this.scorer = scorer;
  }

  @Override
  public CampaignStepType stepType() {
    return CampaignStepType.VIEW;
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
