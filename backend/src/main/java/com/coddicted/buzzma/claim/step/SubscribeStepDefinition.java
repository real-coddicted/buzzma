package com.coddicted.buzzma.claim.step;

import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import com.coddicted.buzzma.claim.scorer.ClaimScreenshotScorer;
import com.coddicted.buzzma.claim.scorer.SubscribeScreenshotScorer;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class SubscribeStepDefinition implements StepDefinition {

  private final SubscribeScreenshotScorer scorer;

  public SubscribeStepDefinition(final SubscribeScreenshotScorer scorer) {
    this.scorer = scorer;
  }

  @Override
  public CampaignStepType stepType() {
    return CampaignStepType.SUBSCRIBE;
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
