package com.coddicted.buzzma.claim.service.impl;

import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.persistence.ClaimScreenshotRepository;
import com.coddicted.buzzma.claim.service.ClaimScreenshotService;
import com.coddicted.buzzma.extraction.entity.ExtractionJob;
import com.coddicted.buzzma.extraction.entity.ExtractionResult;
import com.coddicted.buzzma.extraction.entity.RatingExtractionResult;
import com.coddicted.buzzma.extraction.entity.ReturnExtractionResult;
import com.coddicted.buzzma.extraction.entity.ReviewExtractionResult;
import com.coddicted.buzzma.extraction.entity.ValidationError;
import com.coddicted.buzzma.extraction.service.ExtractionResultValidator;
import com.coddicted.buzzma.extraction.service.GeminiExtractionPromptBuilder;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import com.coddicted.buzzma.shared.gemini.GeminiClient;
import com.coddicted.buzzma.shared.gemini.GeminiException;
import com.coddicted.buzzma.storage.service.StorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

  public ClaimScreenshotServiceImpl(
      final ClaimScreenshotRepository screenshotRepository,
      final GeminiClient geminiClient,
      final GeminiExtractionPromptBuilder promptBuilder,
      final ExtractionResultValidator validator,
      final StorageService storageService,
      final ObjectMapper objectMapper) {
    this.screenshotRepository = screenshotRepository;
    this.geminiClient = geminiClient;
    this.promptBuilder = promptBuilder;
    this.validator = validator;
    this.storageService = storageService;
    this.objectMapper = objectMapper;
  }

  @Override
  @Transactional
  public ExtractionResult extractSync(
      final byte[] imageBytes,
      final String originalFilename,
      final String contentType,
      final UUID requesterId) {
    LOGGER.debug("extractSync: starting for requester {}", requesterId);

    final ExtractionResult result;
    try {
      result = callGemini(imageBytes, contentType);
    } catch (final GeminiException e) {
      LOGGER.warn(
          "extractSync: Gemini call failed for requester {}: {}", requesterId, e.getMessage());
      throw new BusinessRuleViolationException("Extraction failed: " + e.getMessage());
    }

    final List<ValidationError> errors = this.validator.validate(result);
    if (!errors.isEmpty()) {
      final String errorSummary =
          errors.stream()
              .map(ve -> ve.getField() + ": " + ve.getMessage())
              .collect(Collectors.joining("; "));
      LOGGER.warn("extractSync: validation failed for requester {}: {}", requesterId, errorSummary);
      result.getValidationErrors().addAll(errors);
    }

    return result;
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

    final Map<String, String> details = new HashMap<>();
    details.put("productName", extracted.getProductName());
    details.put("accountName", extracted.getAccountName());
    details.put(
        "rating", extracted.getRating() != null ? String.valueOf(extracted.getRating()) : null);

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

    final Map<String, String> details = new HashMap<>();
    details.put("productName", extracted.getProductName());
    details.put("reviewText", extracted.getReviewText());
    details.put("accountName", extracted.getAccountName());
    details.put("reviewDate", extracted.getReviewDate());

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

    final Map<String, String> details = new HashMap<>();
    details.put("productName", extracted.getProductName());
    details.put("accountName", extracted.getAccountName());
    details.put("returnWindowClosedText", extracted.getReturnWindowClosedText());
    details.put("returnWindowClosedDate", extracted.getReturnWindowClosedDate());

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
}
