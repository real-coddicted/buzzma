package com.coddicted.buzzma.claim.service.impl;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.claim.client.ExtractedScoredResult;
import com.coddicted.buzzma.claim.client.GeminiClientProxy;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.persistence.ClaimRepository;
import com.coddicted.buzzma.claim.persistence.ClaimScreenshotRepository;
import com.coddicted.buzzma.claim.processor.ClaimScreenshotProcessor;
import com.coddicted.buzzma.claim.scorer.ClaimScreenshotScorer;
import com.coddicted.buzzma.claim.service.ClaimScreenshotService;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.claim.step.StepDefinition;
import com.coddicted.buzzma.claim.step.StepDefinitionRegistry;
import com.coddicted.buzzma.extraction.entity.ExtractionJob;
import com.coddicted.buzzma.extraction.entity.ExtractionResult;
import com.coddicted.buzzma.extraction.entity.ScoredValue;
import com.coddicted.buzzma.extraction.entity.ValidationError;
import com.coddicted.buzzma.scoring.entity.ScoringJob;
import com.coddicted.buzzma.shared.constants.BuzzmahConstants;
import com.coddicted.buzzma.shared.enums.Platform;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClaimScreenshotServiceImpl implements ClaimScreenshotService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClaimScreenshotServiceImpl.class);

  private final ClaimScreenshotProcessor processor;
  private final ClaimScreenshotRepository screenshotRepository;
  private final ClaimRepository claimRepository;
  private final GeminiClientProxy geminiClientProxy;
  private final CampaignService campaignService;
  private final StepDefinitionRegistry stepDefinitionRegistry;
  private final ClaimService claimService;

  public ClaimScreenshotServiceImpl(
      final ClaimScreenshotProcessor processor,
      final ClaimScreenshotRepository screenshotRepository,
      final ClaimRepository claimRepository,
      final GeminiClientProxy geminiClientProxy,
      final CampaignService campaignService,
      final StepDefinitionRegistry stepDefinitionRegistry,
      final ClaimService claimService) {
    this.processor = processor;
    this.screenshotRepository = screenshotRepository;
    this.claimRepository = claimRepository;
    this.geminiClientProxy = geminiClientProxy;
    this.campaignService = campaignService;
    this.stepDefinitionRegistry = stepDefinitionRegistry;
    this.claimService = claimService;
  }

  @Override
  @Transactional
  public ExtractionResult extractSync(
      final CampaignStepType stepType,
      final byte[] imageBytes,
      final String originalFilename,
      final String contentType,
      final UUID requesterId,
      final UUID campaignId) {
    LOGGER.debug(
        "extractSync: starting for requester {}, campaign {}, step {}",
        requesterId,
        campaignId,
        stepType);

    final StepDefinition stepDefinition = this.stepDefinitionRegistry.get(stepType);
    final String prompt =
        stepDefinition
            .extractionPrompt()
            .orElseThrow(
                () ->
                    new IllegalStateException("Step " + stepType + " does not support extraction"));
    final Map<String, String> extracted =
        this.geminiClientProxy.extract(prompt, imageBytes, contentType);

    final List<ValidationError> errors = stepDefinition.validate(extracted);
    if (!errors.isEmpty()) {
      final String errorSummary =
          errors.stream()
              .map(ve -> ve.getField() + ": " + ve.getMessage())
              .collect(Collectors.joining("; "));
      LOGGER.warn("extractSync: validation failed for requester {}: {}", requesterId, errorSummary);
    }

    this.campaignService.getById(campaignId); // fail fast if the campaign doesn't exist

    return ExtractionResult.builder()
        .platform(Platform.parse(extracted.get(BuzzmahConstants.PLATFORM)))
        .orderId(extracted.get(BuzzmahConstants.ORDER_ID))
        .orderDate(extracted.get(BuzzmahConstants.ORDER_DATE))
        .productName(extracted.get(BuzzmahConstants.PRODUCT_NAME))
        .sellerName(extracted.get(BuzzmahConstants.SELLER_NAME))
        .amount(parseAmount(extracted.get(BuzzmahConstants.AMOUNT)))
        .orderedBy(extracted.get(BuzzmahConstants.ORDERED_BY))
        .validationErrors(errors)
        .extractedResult(toScoredValues(extracted))
        .build();
  }

  private BigDecimal parseAmount(final String raw) {
    if (raw == null) {
      return null;
    }
    try {
      return new BigDecimal(raw);
    } catch (final NumberFormatException e) {
      return null;
    }
  }

  @Override
  @Transactional
  public boolean process(final ExtractionJob job) {
    final UUID claimScreenshotId = job.getClaimScreenshotId();
    final ClaimScreenshot screenshot =
        this.screenshotRepository
            .findById(claimScreenshotId)
            .orElseThrow(
                () -> new NotFoundException("ClaimScreenshot not found: " + claimScreenshotId));
    this.processor.process(job, screenshot);

    final boolean scoringRequired =
        this.stepDefinitionRegistry.get(screenshot.getType()).scoringRequired();
    if (!scoringRequired) {
      // No ScoringJob will follow for this step, so processScoring's updateClaimScore call never
      // runs for it — re-affirm here instead, so the claim's aggregate score isn't left stale
      // after a step completes.
      this.claimService.updateClaimScore(screenshot.getClaimId());
    }
    return scoringRequired;
  }

  @Override
  @Transactional
  public void processScoring(final ScoringJob job) {
    final UUID claimScreenshotId = job.getClaimScreenshotId();
    final ClaimScreenshot screenshot =
        this.screenshotRepository
            .findById(claimScreenshotId)
            .orElseThrow(
                () -> new NotFoundException("ClaimScreenshot not found: " + claimScreenshotId));

    // findByIdForUpdate both serializes concurrent scoring of screenshots belonging to the same
    // claim (across threads and horizontally-scaled instances), so the score-aggregation UPDATE
    // below never races with another screenshot's aggregation for the same claim, and supplies
    // the Claim the scorer needs.
    final Claim claim =
        this.claimRepository
            .findByIdForUpdate(screenshot.getClaimId())
            .orElseThrow(
                () -> new NotFoundException("Claim not found: " + screenshot.getClaimId()));
    final Campaign campaign = this.campaignService.getById(claim.getCampaignId());

    final ClaimScreenshotScorer scorer =
        this.stepDefinitionRegistry.get(screenshot.getType()).scorer();
    final ExtractedScoredResult result = scorer.score(claim, campaign, screenshot);

    screenshot.setExtractedDetails(result.extractedResult());
    screenshot.setScore(result.overallScore());
    this.screenshotRepository.save(screenshot);

    this.claimService.updateClaimScore(screenshot.getClaimId());
  }

  /**
   * extractSync runs before a {@code Claim} exists, and every {@code ClaimScreenshotScorer} needs
   * one to compare against, so scoring isn't possible here regardless of step type — this returns
   * raw extracted values unscored, matching what the async scheduler path ({@code
   * GenericScreenshotProcessor}) stores before its own later, claim-bound scoring step.
   */
  private Map<String, ScoredValue> toScoredValues(final Map<String, String> extracted) {
    final Map<String, ScoredValue> result = new HashMap<>();
    extracted.forEach(
        (key, value) ->
            result.put(key, ScoredValue.builder().extractedValue(value).score(null).build()));
    return result;
  }
}
