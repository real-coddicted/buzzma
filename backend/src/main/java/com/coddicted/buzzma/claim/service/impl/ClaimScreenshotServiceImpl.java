package com.coddicted.buzzma.claim.service.impl;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.claim.client.ExtractedScoredResult;
import com.coddicted.buzzma.claim.client.GeminiClientProxy;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.entity.ScreenshotType;
import com.coddicted.buzzma.claim.persistence.ClaimScreenshotRepository;
import com.coddicted.buzzma.claim.processor.ClaimScreenshotProcessor;
import com.coddicted.buzzma.claim.scorer.ClaimScreenshotScorer;
import com.coddicted.buzzma.claim.scorer.OrderScreenshotScorer;
import com.coddicted.buzzma.claim.service.ClaimScreenshotService;
import com.coddicted.buzzma.extraction.entity.ExtractionJob;
import com.coddicted.buzzma.extraction.entity.ExtractionResult;
import com.coddicted.buzzma.extraction.entity.ScoredValue;
import com.coddicted.buzzma.extraction.entity.ValidationError;
import com.coddicted.buzzma.extraction.service.ExtractionResultValidator;
import com.coddicted.buzzma.scoring.entity.ScoringJob;
import com.coddicted.buzzma.shared.constants.BuzzmahConstants;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClaimScreenshotServiceImpl implements ClaimScreenshotService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClaimScreenshotServiceImpl.class);

  private final ClaimScreenshotProcessor processor;
  private final ClaimScreenshotScorer scorer;
  private final ClaimScreenshotRepository screenshotRepository;
  private final GeminiClientProxy geminiClientProxy;
  private final ExtractionResultValidator validator;
  private final CampaignService campaignService;
  private final OrderScreenshotScorer orderScreenshotScorer;

  public ClaimScreenshotServiceImpl(
      @Qualifier("ClaimScreenshotProcessor") final ClaimScreenshotProcessor processor,
      @Qualifier("ClaimScreenshotScorer") final ClaimScreenshotScorer scorer,
      final ClaimScreenshotRepository screenshotRepository,
      final GeminiClientProxy geminiClientProxy,
      final ExtractionResultValidator validator,
      final CampaignService campaignService,
      final OrderScreenshotScorer orderScreenshotScorer) {
    this.processor = processor;
    this.scorer = scorer;
    this.screenshotRepository = screenshotRepository;
    this.geminiClientProxy = geminiClientProxy;
    this.validator = validator;
    this.campaignService = campaignService;
    this.orderScreenshotScorer = orderScreenshotScorer;
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

    final ExtractionResult raw =
        this.geminiClientProxy.extract(
            ScreenshotType.SCREENSHOT_TYPE_ORDER, imageBytes, contentType, ExtractionResult.class);

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
  public void process(final ExtractionJob job) {
    final UUID claimScreenshotId = job.getClaimScreenshotId();
    final ClaimScreenshot screenshot =
        this.screenshotRepository
            .findById(claimScreenshotId)
            .orElseThrow(
                () -> new NotFoundException("ClaimScreenshot not found: " + claimScreenshotId));
    this.processor.process(job, screenshot);
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
    this.scorer.score(job, screenshot);
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
