package com.coddicted.buzzma.claim.processor;

import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_ORDER;
import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_RETURN;
import static com.coddicted.buzzma.claim.processor.Fixtures.CLAIM_ID;
import static com.coddicted.buzzma.claim.processor.Fixtures.IMAGE_BYTES;
import static com.coddicted.buzzma.claim.processor.Fixtures.JOB_ID;
import static com.coddicted.buzzma.claim.processor.Fixtures.MIME_TYPE;
import static com.coddicted.buzzma.claim.processor.Fixtures.SCREENSHOT_ID;
import static com.coddicted.buzzma.claim.processor.Fixtures.STORAGE_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.persistence.ClaimScreenshotRepository;
import com.coddicted.buzzma.extraction.entity.ExtractionJob;
import com.coddicted.buzzma.extraction.entity.ReturnExtractionResult;
import com.coddicted.buzzma.extraction.entity.ScoredValue;
import com.coddicted.buzzma.shared.util.FileUtils;
import com.coddicted.buzzma.storage.service.StorageService;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@ExtendWith(MockitoExtension.class)
class ReturnScreenshotProcessorTest {

  private static final ReturnExtractionResult EXTRACTION_RESULT =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/claim/processor/return-extraction-result.json",
          ReturnExtractionResult.class);

  @Mock private ClaimScreenshotRepository mockScreenshotRepository;
  @Mock private StorageService mockStorageService;

  @Test
  void testCanProcessOnlyMatchesReturnScreenshots() {
    final ReturnScreenshotProcessor processor =
        new ReturnScreenshotProcessor(
            this.mockScreenshotRepository,
            new Fixtures.FixedResultGeminiClientProxy(EXTRACTION_RESULT),
            this.mockStorageService);

    assertTrue(
        processor.canProcess(ClaimScreenshot.builder().type(SCREENSHOT_TYPE_RETURN).build()));
    assertFalse(
        processor.canProcess(ClaimScreenshot.builder().type(SCREENSHOT_TYPE_ORDER).build()));
  }

  @Test
  void testProcessSavesExtractedDetailsFromGeminiResult() {
    final Fixtures.FixedResultGeminiClientProxy geminiClientProxy =
        new Fixtures.FixedResultGeminiClientProxy(EXTRACTION_RESULT);
    final ReturnScreenshotProcessor processor =
        new ReturnScreenshotProcessor(
            this.mockScreenshotRepository, geminiClientProxy, this.mockStorageService);
    when(this.mockStorageService.retrieve(STORAGE_KEY))
        .thenReturn(ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), IMAGE_BYTES));

    final ClaimScreenshot screenshot =
        ClaimScreenshot.builder()
            .id(SCREENSHOT_ID)
            .claimId(CLAIM_ID)
            .storageKey(STORAGE_KEY)
            .type(SCREENSHOT_TYPE_RETURN)
            .build();
    final ExtractionJob job =
        ExtractionJob.builder().id(JOB_ID).claimScreenshotId(SCREENSHOT_ID).build();

    processor.process(job, screenshot);

    assertEquals(SCREENSHOT_TYPE_RETURN, geminiClientProxy.lastScreenshotType);
    assertEquals(MIME_TYPE, geminiClientProxy.lastMimeType);
    assertEquals(ReturnExtractionResult.class, geminiClientProxy.lastValueType);

    final ArgumentCaptor<ClaimScreenshot> captor = ArgumentCaptor.forClass(ClaimScreenshot.class);
    verify(this.mockScreenshotRepository).save(captor.capture());
    final Map<String, ScoredValue> expected =
        Fixtures.loadExtractedDetails(
            "/fixtures/output/claim/processor/return-extracted-details.json");
    assertEquals(expected, captor.getValue().getExtractedDetails());
  }
}
