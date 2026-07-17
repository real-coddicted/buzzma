package com.coddicted.buzzma.claim.service.impl;

import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_ORDER;
import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_RATING;
import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_RETURN;
import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_REVIEW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.Product;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.entity.ScreenshotType;
import com.coddicted.buzzma.claim.persistence.ClaimScreenshotRepository;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.extraction.entity.ExtractionJob;
import com.coddicted.buzzma.extraction.entity.ScoredValue;
import com.coddicted.buzzma.extraction.service.ExtractionResultValidator;
import com.coddicted.buzzma.extraction.service.GeminiExtractionPromptBuilder;
import com.coddicted.buzzma.scoring.entity.ScoringJob;
import com.coddicted.buzzma.shared.constants.BuzzmahConstants;
import com.coddicted.buzzma.shared.enums.Platform;
import com.coddicted.buzzma.shared.gemini.GeminiClient;
import com.coddicted.buzzma.shared.score.LabelScore;
import com.coddicted.buzzma.shared.score.ScoreApiClient;
import com.coddicted.buzzma.shared.score.ScoreRequestDto;
import com.coddicted.buzzma.shared.score.ScoreResponseDto;
import com.coddicted.buzzma.shared.score.ScoringAlgorithm;
import com.coddicted.buzzma.storage.service.StorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@ExtendWith(MockitoExtension.class)
class ClaimScreenshotServiceImplTest {

  private static final UUID CLAIM_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID OWNER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID CAMPAIGN_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
  private static final UUID SCREENSHOT_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
  private static final String STORAGE_KEY = "claims/screenshot.jpg";

  @Mock private ClaimScreenshotRepository mockScreenshotRepository;
  @Mock private GeminiClient mockGeminiClient;
  @Mock private StorageService mockStorageService;
  @Mock private CampaignService mockCampaignService;
  @Mock private ScoreApiClient mockScoreApiClient;
  @Mock private ClaimService mockClaimService;
  @Captor private ArgumentCaptor<List<ScoreRequestDto>> requestCaptor;

  private ClaimScreenshotServiceImpl service;

  @BeforeEach
  void setUp() {
    this.service =
        new ClaimScreenshotServiceImpl(
            this.mockScreenshotRepository,
            this.mockGeminiClient,
            new GeminiExtractionPromptBuilder(),
            new ExtractionResultValidator(),
            this.mockStorageService,
            new ObjectMapper(),
            this.mockCampaignService,
            this.mockScoreApiClient,
            this.mockClaimService);

    when(this.mockStorageService.retrieve(STORAGE_KEY))
        .thenReturn(
            ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), new byte[] {1}));

    final Claim claim =
        Claim.builder()
            .id(CLAIM_ID)
            .campaignId(CAMPAIGN_ID)
            .platform(Platform.PLATFORM_AMAZON)
            .ecommerceOrderId("403-1234567-8901234")
            .accountName("john.doe")
            .build();
    when(this.mockClaimService.getById(CLAIM_ID, OWNER_ID)).thenReturn(claim);

    final Campaign campaign =
        Campaign.builder()
            .id(CAMPAIGN_ID)
            .product(Product.builder().name("Test Product").build())
            .build();
    when(this.mockCampaignService.getById(CAMPAIGN_ID)).thenReturn(campaign);
  }

  private ClaimScreenshot screenshot(final ScreenshotType type) {
    return ClaimScreenshot.builder()
        .id(SCREENSHOT_ID)
        .claimId(CLAIM_ID)
        .createdBy(OWNER_ID)
        .storageKey(STORAGE_KEY)
        .type(type)
        .build();
  }

  private ExtractionJob extractionJob() {
    return ExtractionJob.builder().id(UUID.randomUUID()).claimScreenshotId(SCREENSHOT_ID).build();
  }

  private ScoringJob scoringJob() {
    return ScoringJob.builder().id(UUID.randomUUID()).claimScreenshotId(SCREENSHOT_ID).build();
  }

  private void mockScoreApi(
      final String key, final Map<String, Double> labelScores, final double overallScore) {
    final List<LabelScore> scores =
        labelScores.entrySet().stream()
            .map(e -> LabelScore.builder().label(e.getKey()).score(e.getValue()).build())
            .toList();
    when(this.mockScoreApiClient.score(any(), any()))
        .thenReturn(
            List.of(
                ScoreResponseDto.builder()
                    .key(key)
                    .overallScore(overallScore)
                    .scores(scores)
                    .build()));
  }

  /** Runs the extraction-only phase and returns what got persisted (all scores still null). */
  private ClaimScreenshot extract(final ScreenshotType type) {
    when(this.mockScreenshotRepository.findById(SCREENSHOT_ID))
        .thenReturn(Optional.of(screenshot(type)));
    this.service.process(extractionJob());
    return captureSavedScreenshot();
  }

  /**
   * Runs the scoring-only phase against a screenshot that already has extracted (unscored) details.
   */
  private ClaimScreenshot score(final ClaimScreenshot extracted) {
    when(this.mockScreenshotRepository.findById(SCREENSHOT_ID)).thenReturn(Optional.of(extracted));
    this.service.processScoring(scoringJob());
    return captureSavedScreenshot();
  }

  private ClaimScreenshot captureSavedScreenshot() {
    final ArgumentCaptor<ClaimScreenshot> captor = ArgumentCaptor.forClass(ClaimScreenshot.class);
    verify(this.mockScreenshotRepository, atLeastOnce()).save(captor.capture());
    return captor.getValue();
  }

  @Test
  void testProcessOrderScreenshot() {
    when(this.mockCampaignService.getById(CAMPAIGN_ID))
        .thenReturn(
            Campaign.builder()
                .id(CAMPAIGN_ID)
                .platform(Platform.PLATFORM_AMAZON)
                .startDate(20260101)
                .endDate(20261231)
                .sellerName("Acme Sellers")
                .product(
                    Product.builder()
                        .name("Test Product")
                        .pricePaise(BigInteger.valueOf(50000))
                        .build())
                .build());
    when(this.mockGeminiClient.generateContent(anyString(), any(byte[].class), anyString()))
        .thenReturn(
            "{\"platform\":\"PLATFORM_AMAZON\",\"orderId\":\"403-1234567-8901234\","
                + "\"orderDate\":\"2026-06-15\",\"productName\":\"Test Product\","
                + "\"sellerName\":\"Acme Sellers\",\"amount\":500,\"orderedBy\":\"John Doe\"}");

    final ClaimScreenshot extracted = extract(SCREENSHOT_TYPE_ORDER);
    assertNull(extracted.getScore());
    final Map<String, ScoredValue> extractedDetails = extracted.getExtractedDetails();
    assertNull(extractedDetails.get("orderId").getScore());
    assertNull(extractedDetails.get("orderDate").getScore());
    assertNull(extractedDetails.get("amount").getScore());
    assertNull(extractedDetails.get(BuzzmahConstants.PRODUCT_NAME).getScore());

    mockScoreApi(
        "orderData",
        Map.of(
            BuzzmahConstants.PLATFORM,
            1.0,
            BuzzmahConstants.PRODUCT_NAME,
            1.0,
            BuzzmahConstants.SELLER_NAME,
            1.0),
        0.95);

    final ClaimScreenshot scored = score(extracted);
    // Combined score is the min of orderDate (100) and the API's platform/productName/sellerName
    // scores (all 100), rather than using the API's overallScore (95) verbatim.
    assertEquals(100, scored.getScore());
    final Map<String, ScoredValue> details = scored.getExtractedDetails();
    assertEquals(100, details.get("orderDate").getScore());
    assertNull(details.get("amount").getScore());
    assertEquals(100, details.get(BuzzmahConstants.PLATFORM).getScore());
    assertEquals(100, details.get(BuzzmahConstants.PRODUCT_NAME).getScore());
    assertNull(details.get("orderId").getScore());
  }

  @Test
  void testProcessOrderScreenshot_mismatchedOrderDateAndAmountLowerOverallScore() {
    when(this.mockCampaignService.getById(CAMPAIGN_ID))
        .thenReturn(
            Campaign.builder()
                .id(CAMPAIGN_ID)
                .platform(Platform.PLATFORM_AMAZON)
                .startDate(20260101)
                .endDate(20260301)
                .sellerName("Acme Sellers")
                .product(
                    Product.builder()
                        .name("Test Product")
                        .pricePaise(BigInteger.valueOf(100000))
                        .build())
                .build());
    when(this.mockGeminiClient.generateContent(anyString(), any(byte[].class), anyString()))
        .thenReturn(
            "{\"platform\":\"PLATFORM_AMAZON\",\"orderId\":\"403-1234567-8901234\","
                + "\"orderDate\":\"2026-06-15\",\"productName\":\"Test Product\","
                + "\"sellerName\":\"Acme Sellers\",\"amount\":500,\"orderedBy\":\"John Doe\"}");

    final ClaimScreenshot extracted = extract(SCREENSHOT_TYPE_ORDER);

    // orderDate (2026-06-15) falls outside the campaign window and amount (500) is below the
    // product price (1000), so both local scores are 0.0, even though the Score API rates
    // platform/productName/sellerName perfectly and reports a high overallScore.
    mockScoreApi(
        "orderData",
        Map.of(
            BuzzmahConstants.PLATFORM,
            1.0,
            BuzzmahConstants.PRODUCT_NAME,
            1.0,
            BuzzmahConstants.SELLER_NAME,
            1.0),
        0.95);

    final ClaimScreenshot scored = score(extracted);
    final Map<String, ScoredValue> details = scored.getExtractedDetails();
    assertEquals(0, details.get("orderDate").getScore());
    assertNull(details.get("amount").getScore());
    // Combined score (min of 0, 100, 100, 100) is 0, unlike the
    // API's inflated overallScore of 95 which ignored the mismatched orderDate.
    assertEquals(0, scored.getScore());
  }

  @Test
  void testProcessRatingScreenshot_ratingPresent() {
    when(this.mockGeminiClient.generateContent(anyString(), any(byte[].class), anyString()))
        .thenReturn(
            "{\"platform\":\"PLATFORM_AMAZON\",\"productName\":\"Test Product\","
                + "\"accountName\":\"john.doe\",\"rating\":5}");

    final ClaimScreenshot extracted = extract(SCREENSHOT_TYPE_RATING);
    assertNull(extracted.getScore());
    final Map<String, ScoredValue> extractedDetails = extracted.getExtractedDetails();
    assertEquals(
        "PLATFORM_AMAZON", extractedDetails.get(BuzzmahConstants.PLATFORM).getExtractedValue());
    assertNull(extractedDetails.get(BuzzmahConstants.PLATFORM).getScore());
    assertEquals("5", extractedDetails.get("rating").getExtractedValue());
    assertNull(extractedDetails.get("rating").getScore());
    assertNull(extractedDetails.get(BuzzmahConstants.PRODUCT_NAME).getScore());

    mockScoreApi(
        "ratingData",
        Map.of(BuzzmahConstants.PLATFORM, 1.0, "productName", 1.0, "accountName", 0.8),
        0.9);

    final ClaimScreenshot scored = score(extracted);
    assertEquals(90, scored.getScore());
    final Map<String, ScoredValue> details = scored.getExtractedDetails();
    assertEquals(100, details.get(BuzzmahConstants.PLATFORM).getScore());
    assertEquals(100, details.get("productName").getScore());
    assertEquals(80, details.get("accountName").getScore());
    assertEquals("5", details.get("rating").getExtractedValue());
    assertEquals(100, details.get("rating").getScore());
  }

  @Test
  void testProcessRatingScreenshot_ratingMissing() {
    when(this.mockGeminiClient.generateContent(anyString(), any(byte[].class), anyString()))
        .thenReturn(
            "{\"productName\":\"Test Product\",\"accountName\":\"john.doe\",\"rating\":null}");

    final ClaimScreenshot extracted = extract(SCREENSHOT_TYPE_RATING);
    assertNull(extracted.getExtractedDetails().get("rating").getExtractedValue());

    mockScoreApi("ratingData", Map.of("productName", 1.0, "accountName", 0.8), 0.9);

    final ClaimScreenshot scored = score(extracted);
    final Map<String, ScoredValue> details = scored.getExtractedDetails();
    assertNull(details.get("rating").getExtractedValue());
    assertEquals(0, details.get("rating").getScore());
  }

  @Test
  void testProcessReviewScreenshot_withReviewUrl() {
    when(this.mockGeminiClient.generateContent(anyString(), any(byte[].class), anyString()))
        .thenReturn(
            "{\"platform\":\"PLATFORM_AMAZON\",\"productName\":\"Test Product\",\"reviewText\":\"Great!\","
                + "\"accountName\":\"john.doe\",\"reviewDate\":\"2026-01-01\","
                + "\"reviewUrl\":\"https://amazon.in/review/123\"}");

    final ClaimScreenshot extracted = extract(SCREENSHOT_TYPE_REVIEW);
    assertNull(extracted.getScore());
    assertEquals(
        "https://amazon.in/review/123",
        extracted.getExtractedDetails().get("reviewUrl").getExtractedValue());
    assertNull(extracted.getExtractedDetails().get("reviewUrl").getScore());

    when(this.mockClaimService.getById(CLAIM_ID, OWNER_ID))
        .thenReturn(
            Claim.builder()
                .id(CLAIM_ID)
                .campaignId(CAMPAIGN_ID)
                .platform(Platform.PLATFORM_AMAZON)
                .accountName("john.doe")
                .reviewUrl("https://amazon.in/review/123")
                .build());
    mockScoreApi(
        "reviewData",
        Map.of(
            BuzzmahConstants.PLATFORM,
            1.0,
            "productName",
            1.0,
            "accountName",
            0.8,
            "reviewUrl",
            1.0),
        0.9);

    final ClaimScreenshot scored = score(extracted);

    verify(this.mockScoreApiClient)
        .score(this.requestCaptor.capture(), eq(ScoringAlgorithm.MIN_VALUE));
    assertEquals(4, this.requestCaptor.getValue().get(0).getPayload().size());

    assertEquals(90, scored.getScore());
    final Map<String, ScoredValue> details = scored.getExtractedDetails();
    assertEquals(100, details.get(BuzzmahConstants.PLATFORM).getScore());
    assertEquals("https://amazon.in/review/123", details.get("reviewUrl").getExtractedValue());
    assertEquals(100, details.get("reviewUrl").getScore());
    assertNull(details.get("reviewText").getScore());
  }

  @Test
  void testProcessReviewScreenshot_withoutReviewUrl() {
    when(this.mockGeminiClient.generateContent(anyString(), any(byte[].class), anyString()))
        .thenReturn(
            "{\"platform\":\"PLATFORM_AMAZON\",\"productName\":\"Test Product\",\"reviewText\":\"Great!\","
                + "\"accountName\":\"john.doe\",\"reviewDate\":\"2026-01-01\",\"reviewUrl\":null}");

    final ClaimScreenshot extracted = extract(SCREENSHOT_TYPE_REVIEW);
    assertNull(extracted.getExtractedDetails().get("reviewUrl").getExtractedValue());

    mockScoreApi(
        "reviewData",
        Map.of(BuzzmahConstants.PLATFORM, 1.0, "productName", 1.0, "accountName", 0.8),
        0.85);

    final ClaimScreenshot scored = score(extracted);

    verify(this.mockScoreApiClient)
        .score(this.requestCaptor.capture(), eq(ScoringAlgorithm.MIN_VALUE));
    assertEquals(3, this.requestCaptor.getValue().get(0).getPayload().size());

    final Map<String, ScoredValue> details = scored.getExtractedDetails();
    assertNull(details.get("reviewUrl").getExtractedValue());
    assertEquals(0, details.get("reviewUrl").getScore());
  }

  @Test
  void testProcessReturnScreenshot() {
    when(this.mockGeminiClient.generateContent(anyString(), any(byte[].class), anyString()))
        .thenReturn(
            "{\"platform\":\"PLATFORM_AMAZON\",\"productName\":\"Test Product\",\"accountName\":\"john.doe\","
                + "\"returnWindowClosedText\":\"Return window closed\",\"returnWindowClosedDate\":\"2026-02-01\"}");

    final ClaimScreenshot extracted = extract(SCREENSHOT_TYPE_RETURN);
    assertNull(extracted.getScore());

    mockScoreApi(
        "returnData",
        Map.of(BuzzmahConstants.PLATFORM, 1.0, "productName", 1.0, "accountName", 0.8),
        0.9);

    final ClaimScreenshot scored = score(extracted);
    assertEquals(90, scored.getScore());
    final Map<String, ScoredValue> details = scored.getExtractedDetails();
    assertEquals(100, details.get(BuzzmahConstants.PLATFORM).getScore());
    assertEquals(100, details.get("productName").getScore());
    assertEquals(80, details.get("accountName").getScore());
    assertNull(details.get("returnWindowClosedText").getScore());
  }
}
