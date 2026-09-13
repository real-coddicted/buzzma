package com.coddicted.buzzma.claim.processor;

import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_DELIVERY;
import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_DOWNLOAD_INSTALL;
import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_ORDER;
import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_RATING;
import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_RETURN;
import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_REVIEW;
import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_SELLER_FEEDBACK;
import static com.coddicted.buzzma.claim.processor.Fixtures.CLAIM_ID;
import static com.coddicted.buzzma.claim.processor.Fixtures.IMAGE_BYTES;
import static com.coddicted.buzzma.claim.processor.Fixtures.JOB_ID;
import static com.coddicted.buzzma.claim.processor.Fixtures.MIME_TYPE;
import static com.coddicted.buzzma.claim.processor.Fixtures.SCREENSHOT_ID;
import static com.coddicted.buzzma.claim.processor.Fixtures.STORAGE_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.entity.ScreenshotType;
import com.coddicted.buzzma.claim.persistence.ClaimScreenshotRepository;
import com.coddicted.buzzma.claim.scorer.DeliveryScreenshotScorer;
import com.coddicted.buzzma.claim.scorer.DownloadInstallScreenshotScorer;
import com.coddicted.buzzma.claim.scorer.OrderScreenshotScorer;
import com.coddicted.buzzma.claim.scorer.RatingScreenshotScorer;
import com.coddicted.buzzma.claim.scorer.ReturnScreenshotScorer;
import com.coddicted.buzzma.claim.scorer.ReviewScreenshotScorer;
import com.coddicted.buzzma.claim.scorer.SellerFeedbackScreenshotScorer;
import com.coddicted.buzzma.claim.step.DeliveryStepDefinition;
import com.coddicted.buzzma.claim.step.DownloadInstallStepDefinition;
import com.coddicted.buzzma.claim.step.OrderStepDefinition;
import com.coddicted.buzzma.claim.step.RatingStepDefinition;
import com.coddicted.buzzma.claim.step.ReturnStepDefinition;
import com.coddicted.buzzma.claim.step.ReviewStepDefinition;
import com.coddicted.buzzma.claim.step.SellerFeedbackStepDefinition;
import com.coddicted.buzzma.claim.step.StepDefinitionRegistry;
import com.coddicted.buzzma.extraction.entity.ExtractionJob;
import com.coddicted.buzzma.extraction.entity.ScoredValue;
import com.coddicted.buzzma.extraction.service.GeminiExtractionPromptBuilder;
import com.coddicted.buzzma.storage.service.StorageService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@ExtendWith(MockitoExtension.class)
class GenericScreenshotProcessorTest {

  @Mock private ClaimScreenshotRepository mockScreenshotRepository;
  @Mock private StorageService mockStorageService;
  @Mock private OrderScreenshotScorer mockOrderScreenshotScorer;
  @Mock private RatingScreenshotScorer mockRatingScreenshotScorer;
  @Mock private ReviewScreenshotScorer mockReviewScreenshotScorer;
  @Mock private ReturnScreenshotScorer mockReturnScreenshotScorer;
  @Mock private DeliveryScreenshotScorer mockDeliveryScreenshotScorer;
  @Mock private SellerFeedbackScreenshotScorer mockSellerFeedbackScreenshotScorer;
  @Mock private DownloadInstallScreenshotScorer mockDownloadInstallScreenshotScorer;

  private StepDefinitionRegistry stepDefinitionRegistry;

  @BeforeEach
  void setUp() {
    final GeminiExtractionPromptBuilder promptBuilder = new GeminiExtractionPromptBuilder();
    this.stepDefinitionRegistry =
        new StepDefinitionRegistry(
            List.of(
                new OrderStepDefinition(promptBuilder, this.mockOrderScreenshotScorer),
                new RatingStepDefinition(promptBuilder, this.mockRatingScreenshotScorer),
                new ReviewStepDefinition(promptBuilder, this.mockReviewScreenshotScorer),
                new ReturnStepDefinition(promptBuilder, this.mockReturnScreenshotScorer),
                new DeliveryStepDefinition(promptBuilder, this.mockDeliveryScreenshotScorer),
                new SellerFeedbackStepDefinition(
                    promptBuilder, this.mockSellerFeedbackScreenshotScorer),
                new DownloadInstallStepDefinition(
                    promptBuilder, this.mockDownloadInstallScreenshotScorer)));
  }

  @Test
  void testCanProcessMatchesAnyScreenshotType() {
    final GenericScreenshotProcessor processor =
        new GenericScreenshotProcessor(
            this.mockStorageService,
            new Fixtures.FixedResultGeminiClientProxy(Map.of()),
            this.mockScreenshotRepository,
            this.stepDefinitionRegistry);

    assertTrue(processor.canProcess(ClaimScreenshot.builder().type(SCREENSHOT_TYPE_ORDER).build()));
    assertTrue(
        processor.canProcess(ClaimScreenshot.builder().type(SCREENSHOT_TYPE_RATING).build()));
  }

  @Test
  void testProcessOrderScreenshot() {
    assertExtraction(
        SCREENSHOT_TYPE_ORDER,
        "/fixtures/input/claim/processor/order-extraction-result.json",
        "/fixtures/output/claim/processor/order-extracted-details.json");
  }

  @Test
  void testProcessRatingScreenshot() {
    assertExtraction(
        SCREENSHOT_TYPE_RATING,
        "/fixtures/input/claim/processor/rating-extraction-result.json",
        "/fixtures/output/claim/processor/rating-extracted-details.json");
  }

  @Test
  void testProcessReviewScreenshot() {
    assertExtraction(
        SCREENSHOT_TYPE_REVIEW,
        "/fixtures/input/claim/processor/review-extraction-result.json",
        "/fixtures/output/claim/processor/review-extracted-details.json");
  }

  @Test
  void testProcessReturnScreenshot() {
    assertExtraction(
        SCREENSHOT_TYPE_RETURN,
        "/fixtures/input/claim/processor/return-extraction-result.json",
        "/fixtures/output/claim/processor/return-extracted-details.json");
  }

  @Test
  void testProcessDeliveryScreenshot() {
    assertExtraction(
        SCREENSHOT_TYPE_DELIVERY,
        "/fixtures/input/claim/processor/delivery-extraction-result.json",
        "/fixtures/output/claim/processor/delivery-extracted-details.json");
  }

  @Test
  void testProcessSellerFeedbackScreenshot() {
    assertExtraction(
        SCREENSHOT_TYPE_SELLER_FEEDBACK,
        "/fixtures/input/claim/processor/seller-feedback-extraction-result.json",
        "/fixtures/output/claim/processor/seller-feedback-extracted-details.json");
  }

  /**
   * Download & Install was never wired into the old per-type processor chain (see ClaimReviewConfig
   * before this collapse) — an extraction job for it would have failed with "Error processing job".
   * The generic processor fixes that for free since it dispatches by StepDefinitionRegistry lookup
   * instead of a maintained list.
   */
  @Test
  void testProcessDownloadInstallScreenshot() {
    assertExtraction(
        SCREENSHOT_TYPE_DOWNLOAD_INSTALL,
        "/fixtures/input/claim/processor/download-install-extraction-result.json",
        "/fixtures/output/claim/processor/download-install-extracted-details.json");
  }

  private void assertExtraction(
      final ScreenshotType type, final String inputFixture, final String outputFixture) {
    final Map<String, String> extractionResult = Fixtures.loadExtractionResult(inputFixture);
    final Fixtures.FixedResultGeminiClientProxy geminiClientProxy =
        new Fixtures.FixedResultGeminiClientProxy(extractionResult);
    final GenericScreenshotProcessor processor =
        new GenericScreenshotProcessor(
            this.mockStorageService,
            geminiClientProxy,
            this.mockScreenshotRepository,
            this.stepDefinitionRegistry);
    when(this.mockStorageService.retrieve(STORAGE_KEY))
        .thenReturn(ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), IMAGE_BYTES));

    final ClaimScreenshot screenshot =
        ClaimScreenshot.builder()
            .id(SCREENSHOT_ID)
            .claimId(CLAIM_ID)
            .storageKey(STORAGE_KEY)
            .type(type)
            .build();
    final ExtractionJob job =
        ExtractionJob.builder().id(JOB_ID).claimScreenshotId(SCREENSHOT_ID).build();

    processor.process(job, screenshot);

    assertEquals(
        this.stepDefinitionRegistry.get(type).extractionPrompt().orElseThrow(),
        geminiClientProxy.lastPrompt);
    assertEquals(MIME_TYPE, geminiClientProxy.lastMimeType);

    final ArgumentCaptor<ClaimScreenshot> captor = ArgumentCaptor.forClass(ClaimScreenshot.class);
    verify(this.mockScreenshotRepository).save(captor.capture());
    final Map<String, ScoredValue> expected = Fixtures.loadExtractedDetails(outputFixture);
    assertEquals(expected, captor.getValue().getExtractedDetails());
  }
}
