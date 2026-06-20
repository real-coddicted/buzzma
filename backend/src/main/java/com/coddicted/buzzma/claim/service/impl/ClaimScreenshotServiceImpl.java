package com.coddicted.buzzma.claim.service.impl;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.persistence.ClaimScreenshotRepository;
import com.coddicted.buzzma.claim.service.ClaimScreenshotService;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.extraction.entity.ExtractionJob;
import com.coddicted.buzzma.extraction.entity.ExtractionResult;
import com.coddicted.buzzma.extraction.entity.RatingExtractionResult;
import com.coddicted.buzzma.extraction.entity.ReturnExtractionResult;
import com.coddicted.buzzma.extraction.entity.ReviewExtractionResult;
import com.coddicted.buzzma.extraction.entity.ScoredValue;
import com.coddicted.buzzma.extraction.entity.ValidationError;
import com.coddicted.buzzma.extraction.service.ExtractionResultValidator;
import com.coddicted.buzzma.extraction.service.GeminiExtractionPromptBuilder;
import com.coddicted.buzzma.shared.constants.BuzzmahConstants;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import com.coddicted.buzzma.shared.gemini.GeminiClient;
import com.coddicted.buzzma.shared.gemini.GeminiException;
import com.coddicted.buzzma.shared.score.PayloadItem;
import com.coddicted.buzzma.shared.score.ScoreApiClient;
import com.coddicted.buzzma.shared.score.ScoreRequestDto;
import com.coddicted.buzzma.shared.score.ScoreResponseDto;
import com.coddicted.buzzma.storage.service.StorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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

  private final ClaimScreenshotRepository screenshotRepository;
  private final GeminiClient geminiClient;
  private final GeminiExtractionPromptBuilder promptBuilder;
  private final ExtractionResultValidator validator;
  private final StorageService storageService;
  private final ObjectMapper objectMapper;
  private final CampaignService campaignService;
  private final ScoreApiClient scoreApiClient;
  private final ClaimService claimService;

  public ClaimScreenshotServiceImpl(
      final ClaimScreenshotRepository screenshotRepository,
      final GeminiClient geminiClient,
      final GeminiExtractionPromptBuilder promptBuilder,
      final ExtractionResultValidator validator,
      final StorageService storageService,
      final ObjectMapper objectMapper,
      final CampaignService campaignService,
      final ScoreApiClient scoreApiClient,
      final ClaimService claimService) {
    this.screenshotRepository = screenshotRepository;
    this.geminiClient = geminiClient;
    this.promptBuilder = promptBuilder;
    this.validator = validator;
    this.storageService = storageService;
    this.objectMapper = objectMapper;
    this.campaignService = campaignService;
    this.scoreApiClient = scoreApiClient;
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

    final ExtractionResult raw;
    try {
      raw = callGemini(imageBytes, contentType);
    } catch (final GeminiException e) {
      LOGGER.warn(
          "extractSync: Gemini call failed for requester {}: {}", requesterId, e.getMessage());
      throw new BusinessRuleViolationException("Extraction failed: " + e.getMessage());
    }

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
    switch (screenshot.getType()) {
      case SCREENSHOT_TYPE_ORDER -> processOrderScreenshot(job, screenshot);
      case SCREENSHOT_TYPE_RATING -> processRatingScreenshot(job, screenshot);
      case SCREENSHOT_TYPE_REVIEW -> processReviewScreenshot(job, screenshot);
      case SCREENSHOT_TYPE_RETURN -> processReturnScreenshot(job, screenshot);
      default ->
          LOGGER.warn(
              "process: unsupported screenshot type {} for job {}, skipping",
              screenshot.getType(),
              job.getId());
    }
  }

  private ExtractedScoredResult scoreFields(
      final ExtractionResult result, final Campaign campaign) {
    final Map<String, ScoredValue> map = new HashMap<>();

    // Local match: orderDate
    final double orderDateScore = scoreOrderDate(result.getOrderDate(), campaign);
    map.put(
        "orderDate",
        ScoredValue.builder().extractedValue(result.getOrderDate()).score(orderDateScore).build());

    // Local match: amount
    final double amountScore = scoreAmount(result.getAmount(), campaign);
    map.put(
        "amount",
        ScoredValue.builder()
            .extractedValue(result.getAmount() != null ? result.getAmount().toPlainString() : null)
            .score(amountScore)
            .build());

    // Unscored fields
    map.put(
        "orderId", ScoredValue.builder().extractedValue(result.getOrderId()).score(null).build());
    map.put(
        "orderedBy",
        ScoredValue.builder().extractedValue(result.getOrderedBy()).score(null).build());

    // Score API: platform, productName, sellerName
    final String platformValue = result.getPlatform() != null ? result.getPlatform().name() : null;
    final ScoreRequestDto request =
        ScoreRequestDto.builder()
            .key("orderData")
            .payload(
                List.of(
                    PayloadItem.builder()
                        .label(BuzzmahConstants.PLATFORM_NAME)
                        .expected(
                            campaign.getPlatform() != null ? campaign.getPlatform().name() : "")
                        .actual(platformValue != null ? platformValue : "")
                        .build(),
                    PayloadItem.builder()
                        .label(BuzzmahConstants.PRODUCT_NAME)
                        .expected(campaign.getProduct().getName())
                        .actual(result.getProductName() != null ? result.getProductName() : "")
                        .build(),
                    PayloadItem.builder()
                        .label(BuzzmahConstants.SELLER_NAME)
                        .expected(campaign.getSellerName() != null ? campaign.getSellerName() : "")
                        .actual(result.getSellerName() != null ? result.getSellerName() : "")
                        .build()))
            .build();

    final List<ScoreResponseDto> scoreResponses = this.scoreApiClient.score(List.of(request));
    final ScoreResponseDto orderDataResponse =
        scoreResponses.stream()
            .filter(r -> "orderData".equals(r.getKey()))
            .findFirst()
            .orElseThrow(
                () ->
                    new IllegalStateException("Score API returned no result for key 'orderData'"));

    final Map<String, Double> labelScores =
        orderDataResponse.getScores().stream()
            .collect(Collectors.toMap(ls -> ls.getLabel(), ls -> ls.getScore()));

    map.put(
        BuzzmahConstants.PLATFORM,
        ScoredValue.builder()
            .extractedValue(platformValue)
            .score(labelScores.get(BuzzmahConstants.PLATFORM_NAME))
            .build());
    map.put(
        BuzzmahConstants.PRODUCT_NAME,
        ScoredValue.builder()
            .extractedValue(result.getProductName())
            .score(labelScores.get(BuzzmahConstants.PRODUCT_NAME))
            .build());
    map.put(
        BuzzmahConstants.SELLER_NAME,
        ScoredValue.builder()
            .extractedValue(result.getSellerName())
            .score(labelScores.get(BuzzmahConstants.SELLER_NAME))
            .build());

    return new ExtractedScoredResult(map, orderDataResponse.getOverallScore());
  }

  private double scoreOrderDate(final String orderDate, final Campaign campaign) {
    if (orderDate == null || campaign.getStartDate() == null) {
      return 0.0;
    }
    final int dateInt;
    try {
      final LocalDate date = LocalDate.parse(orderDate);
      dateInt = date.getYear() * 10000 + date.getMonthValue() * 100 + date.getDayOfMonth();
    } catch (final DateTimeParseException e) {
      LOGGER.warn("scoreOrderDate: could not parse orderDate '{}': {}", orderDate, e.getMessage());
      return 0.0;
    }
    if (dateInt < campaign.getStartDate()) {
      return 0.0;
    }
    if (campaign.getEndDate() != null && dateInt > campaign.getEndDate()) {
      return 0.0;
    }
    return 1.0;
  }

  private double scoreAmount(final BigDecimal amount, final Campaign campaign) {
    if (amount == null
        || campaign.getProduct() == null
        || campaign.getProduct().getPricePaise() == null) {
      return 0.0;
    }
    final BigInteger amountPaise = amount.multiply(BigDecimal.valueOf(100)).toBigInteger();
    return amountPaise.compareTo(campaign.getProduct().getPricePaise()) >= 0 ? 1.0 : 0.0;
  }

  private void processOrderScreenshot(final ExtractionJob job, final ClaimScreenshot screenshot) {
    final String storageKey = screenshot.getStorageKey();
    LOGGER.info(
        "processOrderScreenshot: calling Gemini for job {}, screenshot {}, storageKey {}",
        job.getId(),
        screenshot.getId(),
        storageKey);

    final byte[] imageBytes = this.storageService.retrieve(storageKey).asByteArray();
    final String mimeType = mimeTypeFromFilename(storageKey);
    final ExtractionResult extracted = callGemini(imageBytes, mimeType);

    LOGGER.info(
        "processOrderScreenshot: extracted platform={} orderId={} productName={} for screenshot {}",
        extracted.getPlatform(),
        extracted.getOrderId(),
        extracted.getProductName(),
        screenshot.getId());

    Claim claim = this.claimService.getById(screenshot.getClaimId(), screenshot.getCreatedBy());
    Campaign campaign = this.campaignService.getById(claim.getCampaignId());
    final ExtractedScoredResult scoring = scoreFields(extracted, campaign);

    screenshot.setExtractedDetails(scoring.extractedResult);
    screenshot.setScore(scoring.overallScore);
    this.screenshotRepository.save(screenshot);

    LOGGER.info(
        "processOrderScreenshot: saved extracted details for screenshot {}", screenshot.getId());
  }

  private void processRatingScreenshot(final ExtractionJob job, final ClaimScreenshot screenshot) {
    final String storageKey = screenshot.getStorageKey();
    LOGGER.info(
        "processRatingScreenshot: calling Gemini for job {}, screenshot {}, storageKey {}",
        job.getId(),
        screenshot.getId(),
        storageKey);

    final byte[] imageBytes = this.storageService.retrieve(storageKey).asByteArray();
    final String mimeType = mimeTypeFromFilename(storageKey);
    final String rawText =
        this.geminiClient.generateContent(
            this.promptBuilder.buildRatingPrompt(), imageBytes, mimeType);
    final String json = sanitizeJson(rawText);

    final RatingExtractionResult extracted;
    try {
      extracted = this.objectMapper.readValue(json, RatingExtractionResult.class);
    } catch (final Exception e) {
      throw new GeminiException(
          "Failed to parse Gemini rating response as RatingExtractionResult: " + json, e);
    }

    LOGGER.info(
        "processRatingScreenshot: extracted productName={} rating={} accountName={} for screenshot {}",
        extracted.getProductName(),
        extracted.getRating(),
        extracted.getAccountName(),
        screenshot.getId());

    final Map<String, ScoredValue> details = new HashMap<>();
    details.put(
        "productName",
        ScoredValue.builder().extractedValue(extracted.getProductName()).score(null).build());
    details.put(
        "accountName",
        ScoredValue.builder().extractedValue(extracted.getAccountName()).score(null).build());
    details.put(
        "rating",
        ScoredValue.builder()
            .extractedValue(
                extracted.getRating() != null ? String.valueOf(extracted.getRating()) : null)
            .score(null)
            .build());

    screenshot.setExtractedDetails(details);
    this.screenshotRepository.save(screenshot);

    LOGGER.info(
        "processRatingScreenshot: saved extracted details for screenshot {}", screenshot.getId());
  }

  private void processReviewScreenshot(final ExtractionJob job, final ClaimScreenshot screenshot) {
    final String storageKey = screenshot.getStorageKey();
    LOGGER.info(
        "processReviewScreenshot: calling Gemini for job {}, screenshot {}, storageKey {}",
        job.getId(),
        screenshot.getId(),
        storageKey);

    final byte[] imageBytes = this.storageService.retrieve(storageKey).asByteArray();
    final String mimeType = mimeTypeFromFilename(storageKey);
    final String rawText =
        this.geminiClient.generateContent(
            this.promptBuilder.buildReviewPrompt(), imageBytes, mimeType);
    final String json = sanitizeJson(rawText);

    final ReviewExtractionResult extracted;
    try {
      extracted = this.objectMapper.readValue(json, ReviewExtractionResult.class);
    } catch (final Exception e) {
      throw new GeminiException(
          "Failed to parse Gemini review response as ReviewExtractionResult: " + json, e);
    }

    LOGGER.info(
        "processReviewScreenshot: extracted productName={} accountName={} reviewDate={} for screenshot {}",
        extracted.getProductName(),
        extracted.getAccountName(),
        extracted.getReviewDate(),
        screenshot.getId());

    final Map<String, ScoredValue> details = new HashMap<>();
    details.put(
        "productName",
        ScoredValue.builder().extractedValue(extracted.getProductName()).score(null).build());
    details.put(
        "reviewText",
        ScoredValue.builder().extractedValue(extracted.getReviewText()).score(null).build());
    details.put(
        "accountName",
        ScoredValue.builder().extractedValue(extracted.getAccountName()).score(null).build());
    details.put(
        "reviewDate",
        ScoredValue.builder().extractedValue(extracted.getReviewDate()).score(null).build());

    screenshot.setExtractedDetails(details);
    this.screenshotRepository.save(screenshot);

    LOGGER.info(
        "processReviewScreenshot: saved extracted details for screenshot {}", screenshot.getId());
  }

  private void processReturnScreenshot(final ExtractionJob job, final ClaimScreenshot screenshot) {
    final String storageKey = screenshot.getStorageKey();
    LOGGER.info(
        "processReturnScreenshot: calling Gemini for job {}, screenshot {}, storageKey {}",
        job.getId(),
        screenshot.getId(),
        storageKey);

    final byte[] imageBytes = this.storageService.retrieve(storageKey).asByteArray();
    final String mimeType = mimeTypeFromFilename(storageKey);
    final String rawText =
        this.geminiClient.generateContent(
            this.promptBuilder.buildReturnPrompt(), imageBytes, mimeType);
    final String json = sanitizeJson(rawText);

    final ReturnExtractionResult extracted;
    try {
      extracted = this.objectMapper.readValue(json, ReturnExtractionResult.class);
    } catch (final Exception e) {
      throw new GeminiException(
          "Failed to parse Gemini return response as ReturnExtractionResult: " + json, e);
    }

    LOGGER.info(
        "processReturnScreenshot: extracted productName={} accountName={} returnWindowClosedDate={} for screenshot {}",
        extracted.getProductName(),
        extracted.getAccountName(),
        extracted.getReturnWindowClosedDate(),
        screenshot.getId());

    final Map<String, ScoredValue> details = new HashMap<>();
    details.put(
        "productName",
        ScoredValue.builder().extractedValue(extracted.getProductName()).score(null).build());
    details.put(
        "accountName",
        ScoredValue.builder().extractedValue(extracted.getAccountName()).score(null).build());
    details.put(
        "returnWindowClosedText",
        ScoredValue.builder()
            .extractedValue(extracted.getReturnWindowClosedText())
            .score(null)
            .build());
    details.put(
        "returnWindowClosedDate",
        ScoredValue.builder()
            .extractedValue(extracted.getReturnWindowClosedDate())
            .score(null)
            .build());

    screenshot.setExtractedDetails(details);
    this.screenshotRepository.save(screenshot);

    LOGGER.info(
        "processReturnScreenshot: saved extracted details for screenshot {}", screenshot.getId());
  }

  private ExtractionResult callGemini(final byte[] imageBytes, final String mimeType) {
    final String rawText =
        this.geminiClient.generateContent(this.promptBuilder.build(), imageBytes, mimeType);
    final String json = sanitizeJson(rawText);
    try {
      return this.objectMapper.readValue(json, ExtractionResult.class);
    } catch (final Exception e) {
      throw new GeminiException("Failed to parse Gemini response as ExtractionResult: " + json, e);
    }
  }

  private String sanitizeJson(final String raw) {
    String trimmed = raw.strip();
    if (trimmed.startsWith("```")) {
      trimmed = trimmed.replaceFirst("```[a-z]*\\n?", "");
      final int lastFence = trimmed.lastIndexOf("```");
      if (lastFence >= 0) {
        trimmed = trimmed.substring(0, lastFence);
      }
      trimmed = trimmed.strip();
    }
    return trimmed;
  }

  private String mimeTypeFromFilename(final String filename) {
    if (filename == null) {
      return "image/jpeg";
    }
    final String lower = filename.toLowerCase();
    if (lower.endsWith(".png")) {
      return "image/png";
    }
    if (lower.endsWith(".gif")) {
      return "image/gif";
    }
    if (lower.endsWith(".webp")) {
      return "image/webp";
    }
    return "image/jpeg";
  }

  private record ExtractedScoredResult(
      Map<String, ScoredValue> extractedResult, double overallScore) {}
}
