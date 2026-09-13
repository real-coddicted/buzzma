package com.coddicted.buzzma.campaign.step;

import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import com.coddicted.buzzma.claim.scorer.ClaimScreenshotScorer;
import com.coddicted.buzzma.claim.scorer.DownloadInstallScreenshotScorer;
import com.coddicted.buzzma.extraction.service.GeminiExtractionPromptBuilder;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** No scoring rubric defined yet; mirrors {@code DownloadInstallScreenshotScorer}'s no-op. */
@Component
public class DownloadInstallStepDefinition implements StepDefinition {

  private final GeminiExtractionPromptBuilder promptBuilder;
  private final DownloadInstallScreenshotScorer scorer;

  public DownloadInstallStepDefinition(
      final GeminiExtractionPromptBuilder promptBuilder,
      final DownloadInstallScreenshotScorer scorer) {
    this.promptBuilder = promptBuilder;
    this.scorer = scorer;
  }

  @Override
  public CampaignStepType stepType() {
    return CampaignStepType.DOWNLOAD_INSTALL;
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
    return Optional.of(this.promptBuilder.buildDownloadInstallPrompt());
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

  @Override
  public List<StepField> fields() {
    return List.of(
        new StepField("platform", false, true),
        new StepField("productName", false, true),
        new StepField("accountName", false, true),
        new StepField("installStatus", false, true),
        new StepField("appVersion", false, true));
  }
}
