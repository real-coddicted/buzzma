package com.coddicted.buzzma.claim.scorer;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.claim.client.ExtractedScoredResult;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.extraction.entity.ScoredValue;
import java.util.Map;

/**
 * Pure scoring computation: given the already-loaded {@code claim} and {@code campaign} plus the
 * screenshot's extracted fields, returns what to persist. Fetching, saving, and updating the
 * claim's aggregate score are the orchestrator's job (see {@code
 * ClaimScreenshotServiceImpl#processScoring}), not the scorer's — keeping the scorer free of {@code
 * ClaimService} avoids a bean cycle, since {@code StepDefinition} (which owns a scorer) is
 * aggregated by {@code StepDefinitionRegistry}, which {@code ClaimServiceImpl} itself depends on.
 */
public interface ClaimScreenshotScorer {
  ExtractedScoredResult score(Claim claim, Campaign campaign, ClaimScreenshot screenshot);

  /**
   * Claim-free scoring for callers that run before a {@code Claim} exists (see {@code
   * ClaimScreenshotServiceImpl#extractSync}): scores only what the {@code campaign} alone can
   * answer, and skips fields that need the claim's own submitted values (e.g. accountName) as well
   * as the manual-entry reconcile step.
   */
  ExtractedScoredResult score(Campaign campaign, Map<String, ScoredValue> extractedDetails);
}
