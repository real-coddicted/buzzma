package com.coddicted.buzzma.claim.service.impl;

import static com.coddicted.buzzma.claim.entity.ClaimStatus.DELIVERY_PROOF_SUBMITTED;
import static com.coddicted.buzzma.claim.entity.ClaimStatus.ORDERED;
import static com.coddicted.buzzma.claim.entity.ClaimStatus.RATING_SUBMITTED;
import static com.coddicted.buzzma.claim.entity.ClaimStatus.REVIEW_SUBMITTED;
import static com.coddicted.buzzma.claim.entity.ClaimStatus.SELLER_FEEDBACK_SUBMITTED;
import static com.coddicted.buzzma.claim.entity.ClaimStatus.UNDER_REVIEW;
import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_DELIVERY;
import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_ORDER;
import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_RATING;
import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_RETURN;
import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_REVIEW;
import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_SELLER_FEEDBACK;
import static com.coddicted.buzzma.claim.entity.ScreenshotVerificationStatus.SCREENSHOT_VERIFICATION_STATUS_PENDING;
import static com.coddicted.buzzma.claim.service.impl.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignShare;
import com.coddicted.buzzma.campaign.entity.CampaignStatus;
import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import com.coddicted.buzzma.campaign.persistence.CampaignSlotRepository;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.campaign.service.CampaignShareService;
import com.coddicted.buzzma.campaign.service.CampaignStepResolver;
import com.coddicted.buzzma.campaign.service.DealService;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.model.ClaimWithDeal;
import com.coddicted.buzzma.claim.persistence.ClaimRepository;
import com.coddicted.buzzma.claim.persistence.ClaimScreenshotRepository;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.extraction.service.ExtractionService;
import com.coddicted.buzzma.shared.constants.WellKnownSequences;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.exception.ForbiddenException;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import com.coddicted.buzzma.shared.service.CodeGenerationService;
import com.coddicted.buzzma.storage.service.StorageService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ClaimServiceImplTest {

  @Mock private ClaimRepository mockClaimRepository;
  @Mock private ClaimScreenshotRepository mockClaimScreenshotRepository;
  @Mock private CampaignService mockCampaignService;
  @Mock private CampaignShareService mockCampaignShareService;
  @Mock private DealService mockDealService;
  @Mock private CampaignSlotRepository mockCampaignSlotRepository;
  @Mock private CampaignStepResolver mockCampaignStepResolver;
  @Mock private StorageService mockStorageService;
  @Mock private ExtractionService mockExtractionService;
  @Mock private CodeGenerationService mockCodeGenerationService;
  private ClaimServiceImpl claimService;

  @BeforeEach
  void setUp() {
    this.claimService =
        new ClaimServiceImpl(
            this.mockClaimRepository,
            this.mockClaimScreenshotRepository,
            this.mockCampaignService,
            this.mockCampaignShareService,
            this.mockDealService,
            this.mockCampaignSlotRepository,
            this.mockCampaignStepResolver,
            this.mockStorageService,
            this.mockExtractionService,
            this.mockCodeGenerationService);
  }

  @Test
  void testCreateClaim() {
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_1);
    when(this.mockClaimRepository.existsByEcommerceOrderIdAndPlatformAndIsDeletedFalse(
            ECOMMERCE_ORDER_ID, PLATFORM))
        .thenReturn(false);
    when(this.mockCampaignSlotRepository.decrementSlotsAvailableIfPositive(SLOT_ID)).thenReturn(1);
    when(this.mockStorageService.store(
            "claims", SCREENSHOT_FILENAME, CONTENT_TYPE, SCREENSHOT_BYTES))
        .thenReturn(SCREENSHOT_KEY);
    when(this.mockCodeGenerationService.generateCodeFromSequence(WellKnownSequences.CLAIM))
        .thenReturn(CLAIM_CODE);
    when(this.mockCampaignService.getById(CLAIM_INPUT.getCampaignId()))
        .thenReturn(
            Campaign.builder()
                .status(CampaignStatus.CAMPAIGN_STATUS_ACTIVE)
                .sellerName("Acme Sellers")
                .build());
    final ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
    when(this.mockClaimRepository.save(claimCaptor.capture())).thenReturn(CLAIM_1);

    final Claim result =
        this.claimService.createClaim(
            CLAIM_INPUT.toBuilder().sellerName("Extracted Seller").build(),
            SCREENSHOT_BYTES,
            SCREENSHOT_FILENAME,
            CONTENT_TYPE,
            EXTRACTED_DETAILS,
            85);

    assertEquals(CLAIM_1, result);
    final Claim saved = claimCaptor.getValue();
    assertEquals(CLAIM_CODE, saved.getCode());
    assertEquals(ORDERED, saved.getStatus());
    assertEquals(85, saved.getScore());
    assertFalse(saved.getIsDeleted());
    assertEquals(OWNER_ID, saved.getCreatedBy());
    assertEquals(OWNER_ID, saved.getUpdatedBy());
    // Campaign has a configured seller name, so the extracted value is preserved
    assertEquals("Extracted Seller", saved.getSellerName());

    final ArgumentCaptor<ClaimScreenshot> screenshotCaptor =
        ArgumentCaptor.forClass(ClaimScreenshot.class);
    verify(this.mockClaimScreenshotRepository).save(screenshotCaptor.capture());
    final ClaimScreenshot savedScreenshot = screenshotCaptor.getValue();
    assertEquals(CLAIM_ID, savedScreenshot.getClaimId());
    assertEquals(SCREENSHOT_KEY, savedScreenshot.getStorageKey());
    assertEquals(SCREENSHOT_TYPE_ORDER, savedScreenshot.getType());
    assertEquals(SCREENSHOT_VERIFICATION_STATUS_PENDING, savedScreenshot.getVerificationStatus());
    // Extracted details are enriched with mismatch flags; verify the original values are preserved
    assertEquals(
        "403-1234567-8901234",
        savedScreenshot.getExtractedDetails().get("orderId").getExtractedValue());
    assertEquals(
        "Test Product",
        savedScreenshot.getExtractedDetails().get("productName").getExtractedValue());
    assertFalse(savedScreenshot.isDeleted());
    assertEquals(OWNER_ID, savedScreenshot.getCreatedBy());
    assertEquals(OWNER_ID, savedScreenshot.getUpdatedBy());
  }

  @Test
  void testCreateClaimForAppReviewReplacesPlaceholderOrderIdWithDeterministicKey() {
    final Claim input =
        CLAIM_INPUT.toBuilder()
            .ecommerceOrderId(ClaimServiceImpl.APP_REVIEW_ORDER_ID_PLACEHOLDER)
            .build();
    final String expectedOrderId = "APPREVIEW:" + input.getCampaignId() + ":" + input.getOwnerId();

    when(this.mockCampaignService.getById(input.getCampaignId()))
        .thenReturn(
            Campaign.builder()
                .status(CampaignStatus.CAMPAIGN_STATUS_ACTIVE)
                .type(com.coddicted.buzzma.campaign.entity.CampaignType.CAMPAIGN_TYPE_APP_REVIEW)
                .build());
    when(this.mockClaimRepository.existsByEcommerceOrderIdAndPlatformAndIsDeletedFalse(
            expectedOrderId, input.getPlatform()))
        .thenReturn(false);
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_1);
    when(this.mockCampaignSlotRepository.decrementSlotsAvailableIfPositive(SLOT_ID)).thenReturn(1);
    when(this.mockStorageService.store(
            "claims", SCREENSHOT_FILENAME, CONTENT_TYPE, SCREENSHOT_BYTES))
        .thenReturn(SCREENSHOT_KEY);
    when(this.mockCodeGenerationService.generateCodeFromSequence(WellKnownSequences.CLAIM))
        .thenReturn(CLAIM_CODE);
    final ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
    when(this.mockClaimRepository.save(claimCaptor.capture())).thenReturn(CLAIM_1);

    this.claimService.createClaim(
        input, SCREENSHOT_BYTES, SCREENSHOT_FILENAME, CONTENT_TYPE, EXTRACTED_DETAILS, 85);

    assertEquals(expectedOrderId, claimCaptor.getValue().getEcommerceOrderId());
  }

  @Test
  void testCreateClaimDropsSellerNameWhenCampaignHasNoSellerName() {
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_1);
    when(this.mockClaimRepository.existsByEcommerceOrderIdAndPlatformAndIsDeletedFalse(
            ECOMMERCE_ORDER_ID, PLATFORM))
        .thenReturn(false);
    when(this.mockCampaignSlotRepository.decrementSlotsAvailableIfPositive(SLOT_ID)).thenReturn(1);
    when(this.mockStorageService.store(
            "claims", SCREENSHOT_FILENAME, CONTENT_TYPE, SCREENSHOT_BYTES))
        .thenReturn(SCREENSHOT_KEY);
    when(this.mockCodeGenerationService.generateCodeFromSequence(WellKnownSequences.CLAIM))
        .thenReturn(CLAIM_CODE);
    when(this.mockCampaignService.getById(CLAIM_INPUT.getCampaignId()))
        .thenReturn(
            Campaign.builder()
                .status(CampaignStatus.CAMPAIGN_STATUS_ACTIVE)
                .sellerName(null)
                .build());
    final ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
    when(this.mockClaimRepository.save(claimCaptor.capture())).thenReturn(CLAIM_1);

    this.claimService.createClaim(
        CLAIM_INPUT.toBuilder().sellerName("Extracted Seller").build(),
        SCREENSHOT_BYTES,
        SCREENSHOT_FILENAME,
        CONTENT_TYPE,
        EXTRACTED_DETAILS,
        85);

    assertNull(claimCaptor.getValue().getSellerName());
  }

  @Test
  void testCreateClaimWhenCampaignNotActive() {
    when(this.mockCampaignService.getById(CLAIM_INPUT.getCampaignId()))
        .thenReturn(Campaign.builder().status(CampaignStatus.CAMPAIGN_STATUS_CLOSED).build());

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () ->
                this.claimService.createClaim(
                    CLAIM_INPUT,
                    SCREENSHOT_BYTES,
                    SCREENSHOT_FILENAME,
                    CONTENT_TYPE,
                    EXTRACTED_DETAILS,
                    null));

    assertEquals(
        "The campaign is not active anymore. Please go back to deals page and refresh once to"
            + " confirm active deals",
        ex.getMessage());
    verify(this.mockCampaignSlotRepository, never())
        .decrementSlotsAvailableIfPositive(ArgumentMatchers.any());
    verify(this.mockClaimRepository, never()).save(ArgumentMatchers.any());
  }

  @Test
  void testCreateClaimWhenAlreadyClaimed() {
    when(this.mockCampaignService.getById(CLAIM_INPUT.getCampaignId()))
        .thenReturn(Campaign.builder().status(CampaignStatus.CAMPAIGN_STATUS_ACTIVE).build());
    when(this.mockClaimRepository.existsByEcommerceOrderIdAndPlatformAndIsDeletedFalse(
            ECOMMERCE_ORDER_ID, PLATFORM))
        .thenReturn(true);

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () ->
                this.claimService.createClaim(
                    CLAIM_INPUT,
                    SCREENSHOT_BYTES,
                    SCREENSHOT_FILENAME,
                    CONTENT_TYPE,
                    EXTRACTED_DETAILS,
                    null));
    assertEquals("Claim with this Order ID has already been placed", ex.getMessage());
  }

  @Test
  void testSubmitRating() {
    when(this.mockClaimRepository.findByIdAndIsDeletedFalse(CLAIM_ID))
        .thenReturn(Optional.of(CLAIM_1));
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_1);
    when(this.mockCampaignStepResolver.resolve(DEAL_1.getCampaign())).thenReturn(STEPS);
    when(this.mockStorageService.store(
            "claims", SCREENSHOT_FILENAME, CONTENT_TYPE, SCREENSHOT_BYTES))
        .thenReturn(SCREENSHOT_KEY);
    final ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
    when(this.mockClaimRepository.save(claimCaptor.capture())).thenReturn(CLAIM_2);
    when(this.mockClaimScreenshotRepository.save(ArgumentMatchers.any())).thenReturn(SCREENSHOT_1);

    final ClaimWithDeal result =
        this.claimService.submitRating(
            CLAIM_ID, OWNER_ID, SCREENSHOT_BYTES, SCREENSHOT_FILENAME, CONTENT_TYPE);

    assertEquals(CLAIM_2, result.claim());
    assertEquals(DEAL_1, result.deal());
    final Claim saved = claimCaptor.getValue();
    assertEquals(RATING_SUBMITTED, saved.getStatus());
    assertEquals(CampaignStepType.RATING, saved.getCurrentStep());
    assertEquals(OWNER_ID, saved.getUpdatedBy());

    final ArgumentCaptor<ClaimScreenshot> screenshotCaptor =
        ArgumentCaptor.forClass(ClaimScreenshot.class);
    verify(this.mockClaimScreenshotRepository).save(screenshotCaptor.capture());
    assertEquals(CLAIM_ID, screenshotCaptor.getValue().getClaimId());
    assertEquals(SCREENSHOT_TYPE_RATING, screenshotCaptor.getValue().getType());
    verify(this.mockExtractionService).submitJob(SCREENSHOT_1.getId(), OWNER_ID);
  }

  @Test
  void testSubmitRatingWhenWrongStep() {
    when(this.mockClaimRepository.findByIdAndIsDeletedFalse(CLAIM_ID))
        .thenReturn(Optional.of(CLAIM_2));
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_1);
    when(this.mockCampaignStepResolver.resolve(DEAL_1.getCampaign())).thenReturn(STEPS);

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () ->
                this.claimService.submitRating(
                    CLAIM_ID, OWNER_ID, SCREENSHOT_BYTES, SCREENSHOT_FILENAME, CONTENT_TYPE));
    assertEquals("Rating can only be submitted after Order", ex.getMessage());
  }

  @Test
  void testSubmitRatingWhenNotFound() {
    when(this.mockClaimRepository.findByIdAndIsDeletedFalse(CLAIM_ID)).thenReturn(Optional.empty());

    final NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () ->
                this.claimService.submitRating(
                    CLAIM_ID, OWNER_ID, SCREENSHOT_BYTES, SCREENSHOT_FILENAME, CONTENT_TYPE));
    assertEquals("Claim not found: " + CLAIM_ID, ex.getMessage());
  }

  @Test
  void testSubmitReview() {
    when(this.mockClaimRepository.findByIdAndIsDeletedFalse(CLAIM_ID))
        .thenReturn(Optional.of(CLAIM_2));
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_1);
    when(this.mockCampaignStepResolver.resolve(DEAL_1.getCampaign())).thenReturn(STEPS);
    when(this.mockStorageService.store(
            "claims", SCREENSHOT_FILENAME, CONTENT_TYPE, SCREENSHOT_BYTES))
        .thenReturn(SCREENSHOT_KEY);
    final ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
    when(this.mockClaimRepository.save(claimCaptor.capture())).thenReturn(CLAIM_3);
    when(this.mockClaimScreenshotRepository.save(ArgumentMatchers.any())).thenReturn(SCREENSHOT_1);

    final ClaimWithDeal result =
        this.claimService.submitReview(
            CLAIM_ID, OWNER_ID, REVIEW_URL, SCREENSHOT_BYTES, SCREENSHOT_FILENAME, CONTENT_TYPE);

    assertEquals(CLAIM_3, result.claim());
    assertEquals(DEAL_1, result.deal());
    final Claim saved = claimCaptor.getValue();
    assertEquals(REVIEW_SUBMITTED, saved.getStatus());
    assertEquals(CampaignStepType.REVIEW, saved.getCurrentStep());
    assertEquals(REVIEW_URL, saved.getReviewUrl());
    assertEquals(OWNER_ID, saved.getUpdatedBy());

    final ArgumentCaptor<ClaimScreenshot> screenshotCaptor =
        ArgumentCaptor.forClass(ClaimScreenshot.class);
    verify(this.mockClaimScreenshotRepository).save(screenshotCaptor.capture());
    assertEquals(CLAIM_ID, screenshotCaptor.getValue().getClaimId());
    assertEquals(SCREENSHOT_TYPE_REVIEW, screenshotCaptor.getValue().getType());
    verify(this.mockExtractionService).submitJob(SCREENSHOT_1.getId(), OWNER_ID);
  }

  @Test
  void testSubmitReviewWhenWrongStep() {
    when(this.mockClaimRepository.findByIdAndIsDeletedFalse(CLAIM_ID))
        .thenReturn(Optional.of(CLAIM_1));
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_1);
    when(this.mockCampaignStepResolver.resolve(DEAL_1.getCampaign())).thenReturn(STEPS);

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () ->
                this.claimService.submitReview(
                    CLAIM_ID,
                    OWNER_ID,
                    REVIEW_URL,
                    SCREENSHOT_BYTES,
                    SCREENSHOT_FILENAME,
                    CONTENT_TYPE));
    assertEquals("Review can only be submitted after Rating", ex.getMessage());
  }

  @Test
  void testSubmitReviewWhenNotFound() {
    when(this.mockClaimRepository.findByIdAndIsDeletedFalse(CLAIM_ID)).thenReturn(Optional.empty());

    final NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () ->
                this.claimService.submitReview(
                    CLAIM_ID,
                    OWNER_ID,
                    REVIEW_URL,
                    SCREENSHOT_BYTES,
                    SCREENSHOT_FILENAME,
                    CONTENT_TYPE));
    assertEquals("Claim not found: " + CLAIM_ID, ex.getMessage());
  }

  @Test
  @Disabled
  // Todo: fix test case
  void testSubmitReviewWhenNotOwner() {
    when(this.mockClaimRepository.findByIdAndOwnerIdAndIsDeletedFalse(CLAIM_ID, NON_OWNER_ID))
        .thenReturn(Optional.empty());

    final ForbiddenException ex =
        assertThrows(
            ForbiddenException.class,
            () ->
                this.claimService.submitReview(
                    CLAIM_ID,
                    NON_OWNER_ID,
                    REVIEW_URL,
                    SCREENSHOT_BYTES,
                    SCREENSHOT_FILENAME,
                    CONTENT_TYPE));
    assertEquals("Access denied", ex.getMessage());
  }

  @Test
  void testSubmitReturn() {
    when(this.mockClaimRepository.findByIdAndIsDeletedFalse(CLAIM_ID))
        .thenReturn(Optional.of(CLAIM_3));
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_1);
    when(this.mockCampaignStepResolver.resolve(DEAL_1.getCampaign())).thenReturn(STEPS);
    when(this.mockStorageService.store(
            "claims", SCREENSHOT_FILENAME, CONTENT_TYPE, SCREENSHOT_BYTES))
        .thenReturn(SCREENSHOT_KEY);
    final ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
    when(this.mockClaimRepository.save(claimCaptor.capture())).thenReturn(CLAIM_1);
    when(this.mockClaimScreenshotRepository.save(ArgumentMatchers.any())).thenReturn(SCREENSHOT_1);

    final ClaimWithDeal result =
        this.claimService.submitReturn(
            CLAIM_ID, OWNER_ID, SCREENSHOT_BYTES, SCREENSHOT_FILENAME, CONTENT_TYPE);

    assertEquals(CLAIM_1, result.claim());
    assertEquals(DEAL_1, result.deal());
    final Claim saved = claimCaptor.getValue();
    assertEquals(UNDER_REVIEW, saved.getStatus());
    assertEquals(CampaignStepType.RETURN_WINDOW, saved.getCurrentStep());
    assertEquals(OWNER_ID, saved.getUpdatedBy());

    final ArgumentCaptor<ClaimScreenshot> screenshotCaptor =
        ArgumentCaptor.forClass(ClaimScreenshot.class);
    verify(this.mockClaimScreenshotRepository).save(screenshotCaptor.capture());
    assertEquals(CLAIM_ID, screenshotCaptor.getValue().getClaimId());
    assertEquals(SCREENSHOT_TYPE_RETURN, screenshotCaptor.getValue().getType());
    verify(this.mockExtractionService).submitJob(SCREENSHOT_1.getId(), OWNER_ID);
  }

  @Test
  void testSubmitReturnWhenWrongStep() {
    when(this.mockClaimRepository.findByIdAndIsDeletedFalse(CLAIM_ID))
        .thenReturn(Optional.of(CLAIM_2));
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_1);
    when(this.mockCampaignStepResolver.resolve(DEAL_1.getCampaign())).thenReturn(STEPS);

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () ->
                this.claimService.submitReturn(
                    CLAIM_ID, OWNER_ID, SCREENSHOT_BYTES, SCREENSHOT_FILENAME, CONTENT_TYPE));
    assertEquals("Return Window can only be submitted after Review", ex.getMessage());
  }

  @Test
  void testSubmitReturnWhenNotFound() {
    when(this.mockClaimRepository.findByIdAndIsDeletedFalse(CLAIM_ID)).thenReturn(Optional.empty());

    final NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () ->
                this.claimService.submitReturn(
                    CLAIM_ID, OWNER_ID, SCREENSHOT_BYTES, SCREENSHOT_FILENAME, CONTENT_TYPE));
    assertEquals("Claim not found: " + CLAIM_ID, ex.getMessage());
  }

  @Test
  @Disabled
  // Todo: fix test case
  void testSubmitReturnWhenNotOwner() {
    when(this.mockClaimRepository.findByIdAndOwnerIdAndIsDeletedFalse(CLAIM_ID, NON_OWNER_ID))
        .thenReturn(Optional.empty());

    final ForbiddenException ex =
        assertThrows(
            ForbiddenException.class,
            () ->
                this.claimService.submitReturn(
                    CLAIM_ID, NON_OWNER_ID, SCREENSHOT_BYTES, SCREENSHOT_FILENAME, CONTENT_TYPE));
    assertEquals("Access denied", ex.getMessage());
  }

  @Test
  void testSubmitDelivery() {
    when(this.mockClaimRepository.findByIdAndIsDeletedFalse(CLAIM_ID))
        .thenReturn(Optional.of(CLAIM_1));
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_1);
    when(this.mockCampaignStepResolver.resolve(DEAL_1.getCampaign()))
        .thenReturn(STEPS_WITH_DELIVERY);
    when(this.mockStorageService.store(
            "claims", SCREENSHOT_FILENAME, CONTENT_TYPE, SCREENSHOT_BYTES))
        .thenReturn(SCREENSHOT_KEY);
    final ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
    when(this.mockClaimRepository.save(claimCaptor.capture())).thenReturn(CLAIM_2);
    when(this.mockClaimScreenshotRepository.save(ArgumentMatchers.any())).thenReturn(SCREENSHOT_1);

    final ClaimWithDeal result =
        this.claimService.submitDelivery(
            CLAIM_ID, OWNER_ID, SCREENSHOT_BYTES, SCREENSHOT_FILENAME, CONTENT_TYPE);

    assertEquals(CLAIM_2, result.claim());
    assertEquals(DEAL_1, result.deal());
    final Claim saved = claimCaptor.getValue();
    assertEquals(DELIVERY_PROOF_SUBMITTED, saved.getStatus());
    assertEquals(CampaignStepType.DELIVERY, saved.getCurrentStep());
    assertEquals(OWNER_ID, saved.getUpdatedBy());

    final ArgumentCaptor<ClaimScreenshot> screenshotCaptor =
        ArgumentCaptor.forClass(ClaimScreenshot.class);
    verify(this.mockClaimScreenshotRepository).save(screenshotCaptor.capture());
    assertEquals(CLAIM_ID, screenshotCaptor.getValue().getClaimId());
    assertEquals(SCREENSHOT_TYPE_DELIVERY, screenshotCaptor.getValue().getType());
    verify(this.mockExtractionService).submitJob(SCREENSHOT_1.getId(), OWNER_ID);
  }

  @Test
  void testSubmitDeliveryWhenWrongStep() {
    when(this.mockClaimRepository.findByIdAndIsDeletedFalse(CLAIM_ID))
        .thenReturn(Optional.of(CLAIM_2));
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_1);
    when(this.mockCampaignStepResolver.resolve(DEAL_1.getCampaign()))
        .thenReturn(STEPS_WITH_DELIVERY);

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () ->
                this.claimService.submitDelivery(
                    CLAIM_ID, OWNER_ID, SCREENSHOT_BYTES, SCREENSHOT_FILENAME, CONTENT_TYPE));
    assertEquals("Delivery can only be submitted after Order", ex.getMessage());
  }

  @Test
  void testSubmitDeliveryWhenNotFound() {
    when(this.mockClaimRepository.findByIdAndIsDeletedFalse(CLAIM_ID)).thenReturn(Optional.empty());

    final NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () ->
                this.claimService.submitDelivery(
                    CLAIM_ID, OWNER_ID, SCREENSHOT_BYTES, SCREENSHOT_FILENAME, CONTENT_TYPE));
    assertEquals("Claim not found: " + CLAIM_ID, ex.getMessage());
  }

  @Test
  void testSubmitSellerFeedback() {
    when(this.mockClaimRepository.findByIdAndIsDeletedFalse(CLAIM_ID))
        .thenReturn(Optional.of(CLAIM_3));
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_1);
    when(this.mockCampaignStepResolver.resolve(DEAL_1.getCampaign()))
        .thenReturn(STEPS_WITH_SELLER_FEEDBACK);
    when(this.mockStorageService.store(
            "claims", SCREENSHOT_FILENAME, CONTENT_TYPE, SCREENSHOT_BYTES))
        .thenReturn(SCREENSHOT_KEY);
    final ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
    when(this.mockClaimRepository.save(claimCaptor.capture())).thenReturn(CLAIM_1);
    when(this.mockClaimScreenshotRepository.save(ArgumentMatchers.any())).thenReturn(SCREENSHOT_1);

    final ClaimWithDeal result =
        this.claimService.submitSellerFeedback(
            CLAIM_ID, OWNER_ID, SCREENSHOT_BYTES, SCREENSHOT_FILENAME, CONTENT_TYPE);

    assertEquals(CLAIM_1, result.claim());
    assertEquals(DEAL_1, result.deal());
    final Claim saved = claimCaptor.getValue();
    assertEquals(SELLER_FEEDBACK_SUBMITTED, saved.getStatus());
    assertEquals(CampaignStepType.SELLER_FEEDBACK, saved.getCurrentStep());
    assertEquals(OWNER_ID, saved.getUpdatedBy());

    final ArgumentCaptor<ClaimScreenshot> screenshotCaptor =
        ArgumentCaptor.forClass(ClaimScreenshot.class);
    verify(this.mockClaimScreenshotRepository).save(screenshotCaptor.capture());
    assertEquals(CLAIM_ID, screenshotCaptor.getValue().getClaimId());
    assertEquals(SCREENSHOT_TYPE_SELLER_FEEDBACK, screenshotCaptor.getValue().getType());
    verify(this.mockExtractionService).submitJob(SCREENSHOT_1.getId(), OWNER_ID);
  }

  @Test
  void testSubmitSellerFeedbackWhenWrongStep() {
    when(this.mockClaimRepository.findByIdAndIsDeletedFalse(CLAIM_ID))
        .thenReturn(Optional.of(CLAIM_1));
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_1);
    when(this.mockCampaignStepResolver.resolve(DEAL_1.getCampaign()))
        .thenReturn(STEPS_WITH_SELLER_FEEDBACK);

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () ->
                this.claimService.submitSellerFeedback(
                    CLAIM_ID, OWNER_ID, SCREENSHOT_BYTES, SCREENSHOT_FILENAME, CONTENT_TYPE));
    assertEquals("Seller Feedback can only be submitted after Review", ex.getMessage());
  }

  @Test
  void testSubmitSellerFeedbackWhenNotFound() {
    when(this.mockClaimRepository.findByIdAndIsDeletedFalse(CLAIM_ID)).thenReturn(Optional.empty());

    final NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () ->
                this.claimService.submitSellerFeedback(
                    CLAIM_ID, OWNER_ID, SCREENSHOT_BYTES, SCREENSHOT_FILENAME, CONTENT_TYPE));
    assertEquals("Claim not found: " + CLAIM_ID, ex.getMessage());
  }

  @Test
  void testGetById() {
    when(this.mockClaimRepository.findByIdAndIsDeletedFalse(CLAIM_ID))
        .thenReturn(Optional.of(CLAIM_1));

    final Claim result = this.claimService.getById(CLAIM_ID, OWNER_ID);

    assertEquals(CLAIM_1, result);
  }

  @Test
  void testGetByIdWhenNotFound() {
    when(this.mockClaimRepository.findByIdAndIsDeletedFalse(CLAIM_ID)).thenReturn(Optional.empty());

    final NotFoundException ex =
        assertThrows(NotFoundException.class, () -> this.claimService.getById(CLAIM_ID, OWNER_ID));
    assertEquals("Claim not found: " + CLAIM_ID, ex.getMessage());
  }

  @Test
  void testGetByIdWhenCampaignSharedWithBrand() {
    when(this.mockClaimRepository.findByIdAndIsDeletedFalse(CLAIM_ID))
        .thenReturn(Optional.of(CLAIM_1));
    when(this.mockCampaignService.getById(CLAIM_1.getCampaignId()))
        .thenReturn(Campaign.builder().ownerId(OWNER_ID).build());
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_1);
    when(this.mockCampaignShareService.findByCampaignId(CLAIM_1.getCampaignId()))
        .thenReturn(Optional.of(CampaignShare.builder().toUserId(NON_OWNER_ID).build()));

    final Claim result = this.claimService.getById(CLAIM_ID, NON_OWNER_ID);

    assertEquals(CLAIM_1, result);
  }

  @Test
  void testGetByIdWhenCampaignNotSharedWithBrand() {
    when(this.mockClaimRepository.findByIdAndIsDeletedFalse(CLAIM_ID))
        .thenReturn(Optional.of(CLAIM_1));
    when(this.mockCampaignService.getById(CLAIM_1.getCampaignId()))
        .thenReturn(Campaign.builder().ownerId(OWNER_ID).build());
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_1);
    when(this.mockCampaignShareService.findByCampaignId(CLAIM_1.getCampaignId()))
        .thenReturn(Optional.empty());

    final NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> this.claimService.getById(CLAIM_ID, NON_OWNER_ID));
    assertEquals("Claim not found: " + CLAIM_ID, ex.getMessage());
  }

  @Test
  void testGetByCode() {
    when(this.mockClaimRepository.findByCodeAndIsDeletedFalse(CLAIM_CODE))
        .thenReturn(Optional.of(CLAIM_1));

    final Claim result = this.claimService.getByCode(CLAIM_CODE);

    assertEquals(CLAIM_1, result);
  }

  @Test
  void testGetByCodeWhenNotFound() {
    when(this.mockClaimRepository.findByCodeAndIsDeletedFalse(CLAIM_CODE))
        .thenReturn(Optional.empty());

    final NotFoundException ex =
        assertThrows(NotFoundException.class, () -> this.claimService.getByCode(CLAIM_CODE));
    assertEquals("Claim not found: " + CLAIM_CODE, ex.getMessage());
  }

  @Test
  @Disabled
  // Todo: fix test case
  void testGetByIdWhenNotOwner() {
    when(this.mockClaimRepository.findByIdAndOwnerIdAndIsDeletedFalse(CLAIM_ID, NON_OWNER_ID))
        .thenReturn(Optional.empty());

    final ForbiddenException ex =
        assertThrows(
            ForbiddenException.class, () -> this.claimService.getById(CLAIM_ID, NON_OWNER_ID));
    assertEquals("Access denied", ex.getMessage());
  }

  @Test
  void testListByOwner() {
    when(this.mockClaimRepository.findByOwnerIdAndIsDeletedFalse(
            ArgumentMatchers.eq(OWNER_ID), ArgumentMatchers.any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(CLAIM_1, CLAIM_2)));

    final Page<Claim> result = this.claimService.listByOwner(OWNER_ID, 0, 10);

    assertEquals(List.of(CLAIM_1, CLAIM_2), result.getContent());
  }

  @Test
  void testUpdateScreenshotAppliesSellerNameWhenCampaignHasSellerName() {
    when(this.mockClaimRepository.findByIdAndIsDeletedFalse(CLAIM_ID))
        .thenReturn(Optional.of(CLAIM_1));
    when(this.mockClaimScreenshotRepository.findById(SCREENSHOT_ID))
        .thenReturn(Optional.of(SCREENSHOT_1));
    when(this.mockStorageService.store(
            "claims", SCREENSHOT_FILENAME, CONTENT_TYPE, SCREENSHOT_BYTES))
        .thenReturn(SCREENSHOT_KEY);
    when(this.mockClaimScreenshotRepository.save(ArgumentMatchers.any())).thenReturn(SCREENSHOT_1);
    when(this.mockClaimScreenshotRepository.findByClaimIdAndIsDeletedFalseOrderByCreatedAtAsc(
            CLAIM_ID))
        .thenReturn(List.of(SCREENSHOT_1));
    when(this.mockCampaignService.getById(CLAIM_1.getCampaignId()))
        .thenReturn(Campaign.builder().sellerName("Acme Sellers").build());
    final ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
    when(this.mockClaimRepository.save(claimCaptor.capture())).thenReturn(CLAIM_1);
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_1);

    this.claimService.updateScreenshot(
        CLAIM_ID,
        OWNER_ID,
        SCREENSHOT_ID,
        SCREENSHOT_TYPE_ORDER,
        SCREENSHOT_BYTES,
        SCREENSHOT_FILENAME,
        CONTENT_TYPE,
        new ClaimService.OrderUpdateFields(null, null, null, null, "New Seller", null, null),
        null);

    assertEquals("New Seller", claimCaptor.getValue().getSellerName());
    verify(this.mockExtractionService).submitJob(SCREENSHOT_1.getId(), OWNER_ID);
  }

  @Test
  void testUpdateScreenshotDropsSellerNameWhenCampaignHasNoSellerName() {
    when(this.mockClaimRepository.findByIdAndIsDeletedFalse(CLAIM_ID))
        .thenReturn(Optional.of(CLAIM_1));
    when(this.mockClaimScreenshotRepository.findById(SCREENSHOT_ID))
        .thenReturn(Optional.of(SCREENSHOT_1));
    when(this.mockStorageService.store(
            "claims", SCREENSHOT_FILENAME, CONTENT_TYPE, SCREENSHOT_BYTES))
        .thenReturn(SCREENSHOT_KEY);
    when(this.mockClaimScreenshotRepository.save(ArgumentMatchers.any())).thenReturn(SCREENSHOT_1);
    when(this.mockClaimScreenshotRepository.findByClaimIdAndIsDeletedFalseOrderByCreatedAtAsc(
            CLAIM_ID))
        .thenReturn(List.of(SCREENSHOT_1));
    when(this.mockCampaignService.getById(CLAIM_1.getCampaignId()))
        .thenReturn(Campaign.builder().sellerName(null).build());
    final ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
    when(this.mockClaimRepository.save(claimCaptor.capture())).thenReturn(CLAIM_1);
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_1);

    this.claimService.updateScreenshot(
        CLAIM_ID,
        OWNER_ID,
        SCREENSHOT_ID,
        SCREENSHOT_TYPE_ORDER,
        SCREENSHOT_BYTES,
        SCREENSHOT_FILENAME,
        CONTENT_TYPE,
        new ClaimService.OrderUpdateFields(null, null, null, null, "New Seller", null, null),
        null);

    assertNull(claimCaptor.getValue().getSellerName());
  }

  @Test
  void testListScreenshots() {
    when(this.mockClaimScreenshotRepository.findByClaimIdAndIsDeletedFalseOrderByCreatedAtAsc(
            CLAIM_ID))
        .thenReturn(List.of(SCREENSHOT_1));

    final List<ClaimScreenshot> result = this.claimService.listScreenshots(CLAIM_ID);

    assertEquals(List.of(SCREENSHOT_1), result);
  }

  private static final List<CampaignStepType> STEPS =
      List.of(
          CampaignStepType.ORDER,
          CampaignStepType.RATING,
          CampaignStepType.REVIEW,
          CampaignStepType.RETURN_WINDOW,
          CampaignStepType.CASHBACK);

  private static final List<CampaignStepType> STEPS_WITH_DELIVERY =
      List.of(
          CampaignStepType.ORDER,
          CampaignStepType.DELIVERY,
          CampaignStepType.RATING,
          CampaignStepType.REVIEW,
          CampaignStepType.RETURN_WINDOW,
          CampaignStepType.CASHBACK);

  private static final List<CampaignStepType> STEPS_WITH_SELLER_FEEDBACK =
      List.of(
          CampaignStepType.ORDER,
          CampaignStepType.RATING,
          CampaignStepType.REVIEW,
          CampaignStepType.SELLER_FEEDBACK,
          CampaignStepType.RETURN_WINDOW,
          CampaignStepType.CASHBACK);
}
