package com.coddicted.buzzma.claim.processor;

import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_RATING;

import com.coddicted.buzzma.claim.client.GeminiClientProxy;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.persistence.ClaimScreenshotRepository;
import com.coddicted.buzzma.claim.utils.ClaimScreenshotProcessorUtils;
import com.coddicted.buzzma.extraction.entity.ExtractionJob;
import com.coddicted.buzzma.extraction.entity.RatingExtractionResult;
import com.coddicted.buzzma.extraction.entity.ScoredValue;
import com.coddicted.buzzma.shared.constants.BuzzmahConstants;
import com.coddicted.buzzma.storage.service.StorageService;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RatingScreenshotProcessor implements ClaimScreenshotProcessor {
  private static final Logger LOGGER = LoggerFactory.getLogger(RatingScreenshotProcessor.class);

  private final StorageService storageService;
  private final GeminiClientProxy geminiClientProxy;
  private final ClaimScreenshotRepository screenshotRepository;

  public RatingScreenshotProcessor(
      final ClaimScreenshotRepository screenshotRepository,
      final GeminiClientProxy geminiClientProxy,
      final StorageService storageService) {
    this.screenshotRepository = screenshotRepository;
    this.geminiClientProxy = geminiClientProxy;
    this.storageService = storageService;
  }

  @Override
  public boolean canProcess(final ClaimScreenshot screenshot) {
    return SCREENSHOT_TYPE_RATING == screenshot.getType();
  }

  @Override
  public void process(final ExtractionJob job, final ClaimScreenshot screenshot) {
    final String storageKey = screenshot.getStorageKey();
    LOGGER.info(
        "processRatingScreenshot: calling Gemini for job {}, screenshot {}, storageKey {}",
        job.getId(),
        screenshot.getId(),
        storageKey);

    final byte[] imageBytes = this.storageService.retrieve(storageKey).asByteArray();
    final String mimeType = ClaimScreenshotProcessorUtils.mimeTypeFromFilename(storageKey);
    final RatingExtractionResult extracted =
        this.geminiClientProxy.extract(
            SCREENSHOT_TYPE_RATING, imageBytes, mimeType, RatingExtractionResult.class);

    LOGGER.info(
        "processRatingScreenshot: extracted productName={} rating={} accountName={} for screenshot {}",
        extracted.getProductName(),
        extracted.getRating(),
        extracted.getAccountName(),
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
        BuzzmahConstants.ACCOUNT_NAME,
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
}
