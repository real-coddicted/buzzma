package com.coddicted.buzzma.claim.scorer;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.claim.client.ExtractedScoredResult;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.extraction.entity.ScoredValue;
import java.util.HashMap;
import java.util.Map;

/**
 * Template for scorers whose {@link #score} shape is: compute a score for the screenshot's
 * extracted fields, merge it into the extracted details, then reconcile the details against the
 * claim's own submitted values. How the score is computed (API call, local rules, or both) is left
 * to subclasses via {@link #computeScoring}, since that step differs per step type (see {@link
 * OrderScreenshotScorer}, which combines a locally-computed date score with the API score instead
 * of just forwarding it).
 */
public abstract class AbstractScoreApiScorer implements ClaimScreenshotScorer {

  @Override
  public final ExtractedScoredResult score(
      final Claim claim, final Campaign campaign, final ClaimScreenshot screenshot) {
    final Map<String, ScoredValue> details = new HashMap<>(screenshot.getExtractedDetails());
    final ExtractedScoredResult scoring = computeScoring(claim, campaign, details);
    details.putAll(scoring.extractedResult());
    return reconcile(claim, details, scoring.overallScore());
  }

  protected abstract ExtractedScoredResult computeScoring(
      Claim claim, Campaign campaign, Map<String, ScoredValue> details);

  protected abstract ExtractedScoredResult reconcile(
      Claim claim, Map<String, ScoredValue> details, Integer overallScore);
}
