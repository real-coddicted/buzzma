package com.coddicted.buzzma.claim.processor;

import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_DELIVERY;

import com.coddicted.buzzma.claim.client.GeminiClientProxy;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.persistence.ClaimScreenshotRepository;
import com.coddicted.buzzma.claim.utils.ClaimScreenshotProcessorUtils;
import com.coddicted.buzzma.extraction.entity.DeliveryExtractionResult;
import com.coddicted.buzzma.extraction.entity.ExtractionJob;
import com.coddicted.buzzma.extraction.entity.ScoredValue;
import com.coddicted.buzzma.shared.constants.BuzzmahConstants;
import com.coddicted.buzzma.storage.service.StorageService;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DeliveryScreenshotProcessor implements ClaimScreenshotProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(DeliveryScreenshotProcessor.class);

  private final StorageService storageService;
  private final GeminiClientProxy geminiClientProxy;
  private final ClaimScreenshotRepository screenshotRepository;

  public DeliveryScreenshotProcessor(
      final ClaimScreenshotRepository screenshotRepository,
      final GeminiClientProxy geminiClientProxy,
      final StorageService storageService) {
    this.screenshotRepository = screenshotRepository;
    this.geminiClientProxy = geminiClientProxy;
    this.storageService = storageService;
  }

  @Override
  public boolean canProcess(final ClaimScreenshot screenshot) {
    return SCREENSHOT_TYPE_DELIVERY == screenshot.getType();
  }

  @Override
  public void process(final ExtractionJob job, final ClaimScreenshot screenshot) {
    final String storageKey = screenshot.getStorageKey();
    LOGGER.info(
        "processDeliveryScreenshot: calling Gemini for job {}, screenshot {}, storageKey {}",
        job.getId(),
        screenshot.getId(),
        storageKey);

    final byte[] imageBytes = this.storageService.retrieve(storageKey).asByteArray();
    final String mimeType = ClaimScreenshotProcessorUtils.mimeTypeFromFilename(storageKey);

    final DeliveryExtractionResult extracted =
        this.geminiClientProxy.extract(
            SCREENSHOT_TYPE_DELIVERY, imageBytes, mimeType, DeliveryExtractionResult.class);

    LOGGER.info(
        "processDeliveryScreenshot: extracted productName={} orderId={} deliveryDate={}"
            + " deliveryStatus={} for screenshot {}",
        extracted.getProductName(),
        extracted.getOrderId(),
        extracted.getDeliveryDate(),
        extracted.getDeliveryStatus(),
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
        BuzzmahConstants.ORDER_ID,
        ScoredValue.builder().extractedValue(extracted.getOrderId()).score(null).build());
    details.put(
        "deliveryDate",
        ScoredValue.builder().extractedValue(extracted.getDeliveryDate()).score(null).build());
    details.put(
        "deliveryStatus",
        ScoredValue.builder().extractedValue(extracted.getDeliveryStatus()).score(null).build());

    screenshot.setExtractedDetails(details);
    this.screenshotRepository.save(screenshot);

    LOGGER.info(
        "processDeliveryScreenshot: saved extracted details for screenshot {}", screenshot.getId());
  }
}
