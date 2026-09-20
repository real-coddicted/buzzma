package com.coddicted.buzzma.claim.processor;

import com.coddicted.buzzma.claim.client.GeminiClientProxy;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.persistence.ClaimScreenshotRepository;
import com.coddicted.buzzma.claim.step.StepDefinition;
import com.coddicted.buzzma.claim.step.StepDefinitionRegistry;
import com.coddicted.buzzma.claim.utils.ClaimScreenshotProcessorUtils;
import com.coddicted.buzzma.extraction.entity.ExtractionJob;
import com.coddicted.buzzma.extraction.entity.ScoredValue;
import com.coddicted.buzzma.storage.service.StorageService;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Runs AI extraction for any step type: the prompt and the set of fields to pull out of Gemini's
 * response both come from that step's {@link StepDefinition}, so adding a new step type needs no
 * new processor class.
 */
@Component
public class GenericScreenshotProcessor implements ClaimScreenshotProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(GenericScreenshotProcessor.class);

  private final StorageService storageService;
  private final GeminiClientProxy geminiClientProxy;
  private final ClaimScreenshotRepository screenshotRepository;
  private final StepDefinitionRegistry stepDefinitionRegistry;

  public GenericScreenshotProcessor(
      final StorageService storageService,
      final GeminiClientProxy geminiClientProxy,
      final ClaimScreenshotRepository screenshotRepository,
      final StepDefinitionRegistry stepDefinitionRegistry) {
    this.storageService = storageService;
    this.geminiClientProxy = geminiClientProxy;
    this.screenshotRepository = screenshotRepository;
    this.stepDefinitionRegistry = stepDefinitionRegistry;
  }

  @Override
  public boolean canProcess(final ClaimScreenshot screenshot) {
    return true;
  }

  @Override
  public void process(final ExtractionJob job, final ClaimScreenshot screenshot) {
    final StepDefinition stepDefinition = this.stepDefinitionRegistry.get(screenshot.getType());
    final String prompt =
        stepDefinition
            .extractionPrompt()
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Step " + stepDefinition.stepType() + " does not support extraction"));

    final String storageKey = screenshot.getStorageKey();
    LOGGER.info(
        "process: calling Gemini for job {}, screenshot {}, storageKey {}",
        job.getId(),
        screenshot.getId(),
        storageKey);

    final byte[] imageBytes = this.storageService.retrieve(storageKey).asByteArray();
    final String mimeType = ClaimScreenshotProcessorUtils.mimeTypeFromFilename(storageKey);
    final Map<String, String> extracted =
        this.geminiClientProxy.extract(prompt, imageBytes, mimeType);

    final Map<String, ScoredValue> details = new HashMap<>();
    stepDefinition
        .fields()
        .forEach(
            field ->
                details.put(
                    field.name(),
                    ScoredValue.builder()
                        .extractedValue(extracted.get(field.name()))
                        .score(null)
                        .build()));

    screenshot.setExtractedDetails(details);
    this.screenshotRepository.save(screenshot);

    LOGGER.info("process: saved extracted details for screenshot {}", screenshot.getId());
  }
}
