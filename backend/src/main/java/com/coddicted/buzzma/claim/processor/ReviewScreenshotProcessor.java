package com.coddicted.buzzma.claim.processor;

import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_REVIEW;

import com.coddicted.buzzma.claim.client.GeminiClientProxy;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.persistence.ClaimScreenshotRepository;
import com.coddicted.buzzma.claim.utils.ClaimScreenshotProcessorUtils;
import com.coddicted.buzzma.extraction.entity.ExtractionJob;
import com.coddicted.buzzma.extraction.entity.ReviewExtractionResult;
import com.coddicted.buzzma.extraction.entity.ScoredValue;
import com.coddicted.buzzma.shared.constants.BuzzmahConstants;
import com.coddicted.buzzma.storage.service.StorageService;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ReviewScreenshotProcessor implements ClaimScreenshotProcessor {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReviewScreenshotProcessor.class);

  private final StorageService storageService;
  private final GeminiClientProxy geminiClientProxy;
  private final ClaimScreenshotRepository screenshotRepository;

  public ReviewScreenshotProcessor(
      final ClaimScreenshotRepository screenshotRepository,
      final GeminiClientProxy geminiClientProxy,
      final StorageService storageService) {
    this.screenshotRepository = screenshotRepository;
    this.geminiClientProxy = geminiClientProxy;
    this.storageService = storageService;
  }

  @Override
  public boolean canProcess(final ClaimScreenshot screenshot) {
    return SCREENSHOT_TYPE_REVIEW == screenshot.getType();
  }

  @Override
  public void process(final ExtractionJob job, final ClaimScreenshot screenshot) {
    final String storageKey = screenshot.getStorageKey();
    LOGGER.info(
        "processReviewScreenshot: calling Gemini for job {}, screenshot {}, storageKey {}",
        job.getId(),
        screenshot.getId(),
        storageKey);

    final byte[] imageBytes = this.storageService.retrieve(storageKey).asByteArray();
    final String mimeType = ClaimScreenshotProcessorUtils.mimeTypeFromFilename(storageKey);
    final ReviewExtractionResult extracted =
        this.geminiClientProxy.extract(
            SCREENSHOT_TYPE_REVIEW, imageBytes, mimeType, ReviewExtractionResult.class);

    LOGGER.info(
        "processReviewScreenshot: extracted productName={} accountName={} reviewDate={} reviewUrl={} for screenshot {}",
        extracted.getProductName(),
        extracted.getAccountName(),
        extracted.getReviewDate(),
        extracted.getReviewUrl(),
        screenshot.getId());

    final String platformValue =
        extracted.getPlatform() != null ? extracted.getPlatform().name() : null;
    final Map<String, ScoredValue> details = new HashMap<>();
    details.put(
        BuzzmahConstants.PLATFORM,
        ScoredValue.builder().extractedValue(platformValue).score(null).build());
    details.put(
        BuzzmahConstants.PRODUCT_NAME,
        ScoredValue.builder().extractedValue(extracted.getProductName()).score(null).build());
    details.put(
        "reviewText",
        ScoredValue.builder().extractedValue(extracted.getReviewText()).score(null).build());
    details.put(
        BuzzmahConstants.ACCOUNT_NAME,
        ScoredValue.builder().extractedValue(extracted.getAccountName()).score(null).build());
    details.put(
        "reviewDate",
        ScoredValue.builder().extractedValue(extracted.getReviewDate()).score(null).build());
    details.put(
        BuzzmahConstants.REVIEW_URL,
        ScoredValue.builder().extractedValue(extracted.getReviewUrl()).score(null).build());

    screenshot.setExtractedDetails(details);
    this.screenshotRepository.save(screenshot);

    LOGGER.info(
        "processReviewScreenshot: saved extracted details for screenshot {}", screenshot.getId());
  }
}
