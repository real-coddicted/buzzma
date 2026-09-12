package com.coddicted.buzzma.claim.template;

import com.coddicted.buzzma.campaign.entity.PromotionCategory;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.extraction.entity.ScoredValue;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Orchestrator to create claims. */
@Component
public class ClaimCreationOrchestrator {

  private final ClaimCreationTemplateRegistry registry;

  public ClaimCreationOrchestrator(final ClaimCreationTemplateRegistry registry) {
    this.registry = registry;
  }

  @Transactional
  public Claim create(
      final PromotionCategory category,
      final Claim partial,
      final byte[] screenshot,
      final String screenshotFilename,
      final String contentType,
      final Map<String, ScoredValue> extractedDetails,
      final Integer overallScore) {
    return this.registry
        .get(category)
        .create(
            partial, screenshot, screenshotFilename, contentType, extractedDetails, overallScore);
  }
}
