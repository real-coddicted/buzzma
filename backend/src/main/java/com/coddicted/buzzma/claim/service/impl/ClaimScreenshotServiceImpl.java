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
import com.coddicted.buzzma.claim.scorer.OrderScreenshotScorer;
import com.coddicted.buzzma.claim.service.ClaimScreenshotService;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.claim.step.StepDefinitionRegistry;
import com.coddicted.buzzma.extraction.entity.ExtractionJob;
import com.coddicted.buzzma.extraction.entity.ExtractionResult;
import com.coddicted.buzzma.extraction.entity.ScoredValue;
import com.coddicted.buzzma.extraction.entity.ValidationError;
import com.coddicted.buzzma.extraction.service.ExtractionResultValidator;
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
  private final ExtractionResultValidator validator;
  private final CampaignService campaignService;
  private final OrderScreenshotScorer orderScreenshotScorer;
  private final StepDefinitionRegistry stepDefinitionRegistry;
  private final ClaimService claimService;

  public ClaimScreenshotServiceImpl(
      final ClaimScreenshotProcessor processor,
      final ClaimScreenshotRepository screenshotRepository,
      final ClaimRepository claimRepository,
      final GeminiClientProxy geminiClientProxy,
      final ExtractionResultValidator validator,
      final CampaignService campaignService,
      final OrderScreenshotScorer orderScreenshotScorer,
      final StepDefinitionRegistry stepDefinitionRegistry,
      final ClaimService claimService) {
    this.processor = processor;
    this.screenshotRepository = screenshotRepository;
    this.claimRepository = claimRepository;
    this.geminiClientProxy = geminiClientProxy;
    this.validator = validator;
    this.campaignService = campaignService;
    this.orderScreenshotScorer = orderScreenshotScorer;
    this.stepDefinitionRegistry = stepDefinitionRegistry;
    this.claimService = claimService;
  }

  @Override
  @Transactional
  public ExtractionResult extractSync(
      final byte[] imageBytes,
      final String originalFilename,
      final String contentType,
      final UUID requesterId,
      final UUID campaignId) {
    LOGGER.debug("extractSync: starting for requester {}, campaign {}", requesterId, campaignId);

    final String prompt =
        this.stepDefinitionRegistry
            .get(CampaignStepType.ORDER)
            .extractionPrompt()
            .orElseThrow(() -> new IllegalStateException("Order step has no extraction prompt"));
    final Map<String, String> extracted =
        this.geminiClientProxy.extract(prompt, imageBytes, contentType);
    final ExtractionResult raw =
        ExtractionResult.builder()
            .platform(
                extracted.get("platform") != null
                    ? Platform.valueOf(extracted.get("platform"))
                    : null)
            .orderId(extracted.get("orderId"))
            .orderDate(extracted.get("orderDate"))
            .productName(extracted.get("productName"))
            .sellerName(extracted.get("sellerName"))
            .amount(
                extracted.get("amount") != null ? new BigDecimal(extracted.get("amount")) : null)
            .orderedBy(extracted.get("orderedBy"))
            .build();

    final List<ValidationError> errors = this.validator.validate(raw);
    if (!errors.isEmpty()) {
      final String errorSummary =
          errors.stream()
              .map(ve -> ve.getField() + ": " + ve.getMessage())
              .collect(Collectors.joining("; "));
      LOGGER.warn("extractSync: validation failed for requester {}: {}", requesterId, errorSummary);
    }

    final Campaign campaign = this.campaignService.getById(campaignId);
    final ExtractedScoredResult scoring = scoreFields(raw, campaign);

    return ExtractionResult.builder()
        .platform(raw.getPlatform())
        .orderId(raw.getOrderId())
        .orderDate(raw.getOrderDate())
        .productName(raw.getProductName())
        .sellerName(raw.getSellerName())
        .amount(raw.getAmount())
        .orderedBy(raw.getOrderedBy())
        .validationErrors(errors)
        .extractedResult(scoring.extractedResult())
        .overallScore(scoring.overallScore())
        .build();
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
    return this.stepDefinitionRegistry.get(screenshot.getType()).scoringRequired();
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

  private ExtractedScoredResult scoreFields(
      final ExtractionResult result, final Campaign campaign) {
    final String platformValue = result.getPlatform() != null ? result.getPlatform().name() : null;

    final ExtractedScoredResult fieldScoring =
        this.orderScreenshotScorer.scoreFields(
            platformValue,
            result.getProductName(),
            result.getSellerName(),
            result.getOrderDate(),
            campaign);

    final Map<String, ScoredValue> map = new HashMap<>(fieldScoring.extractedResult());

    // Unscored fields
    map.put(
        BuzzmahConstants.AMOUNT,
        ScoredValue.builder()
            .extractedValue(result.getAmount() != null ? result.getAmount().toPlainString() : null)
            .score(null)
            .build());
    map.put(
        BuzzmahConstants.ORDER_ID,
        ScoredValue.builder().extractedValue(result.getOrderId()).score(null).build());
    map.put(
        BuzzmahConstants.ORDERED_BY,
        ScoredValue.builder().extractedValue(result.getOrderedBy()).score(null).build());

    return new ExtractedScoredResult(map, fieldScoring.overallScore());
  }
}
