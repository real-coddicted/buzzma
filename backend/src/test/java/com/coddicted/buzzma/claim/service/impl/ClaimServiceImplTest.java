package com.coddicted.buzzma.claim.service.impl;

import static com.coddicted.buzzma.claim.entity.ClaimStatus.ORDERED;
import static com.coddicted.buzzma.claim.entity.ClaimStatus.RATING_SUBMITTED;
import static com.coddicted.buzzma.claim.entity.ClaimStatus.REVIEW_SUBMITTED;
import static com.coddicted.buzzma.claim.entity.ClaimStatus.UNDER_REVIEW;
import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_ORDER;
import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_RATING;
import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_RETURN;
import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_REVIEW;
import static com.coddicted.buzzma.claim.entity.ScreenshotVerificationStatus.SCREENSHOT_VERIFICATION_STATUS_PENDING;
import static com.coddicted.buzzma.claim.service.impl.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import com.coddicted.buzzma.campaign.entity.CampaignTypeStep;
import com.coddicted.buzzma.campaign.entity.CampaignTypeStepId;
import com.coddicted.buzzma.campaign.persistence.CampaignSlotRepository;
import com.coddicted.buzzma.campaign.service.CampaignTypeStepService;
import com.coddicted.buzzma.campaign.service.DealService;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.model.ClaimWithDeal;
import com.coddicted.buzzma.claim.persistence.ClaimRepository;
import com.coddicted.buzzma.claim.persistence.ClaimScreenshotRepository;
import com.coddicted.buzzma.extraction.service.ExtractionService;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.exception.ForbiddenException;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import com.coddicted.buzzma.storage.service.StorageService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClaimServiceImplTest {

  @Mock private ClaimRepository mockClaimRepository;
  @Mock private ClaimScreenshotRepository mockClaimScreenshotRepository;
  @Mock private DealService mockDealService;
  @Mock private CampaignSlotRepository mockCampaignSlotRepository;
  @Mock private CampaignTypeStepService mockCampaignTypeStepService;
  @Mock private StorageService mockStorageService;
  @Mock private ExtractionService mockExtractionService;
  private ClaimServiceImpl claimService;

  @BeforeEach
  void setUp() {
    this.claimService =
        new ClaimServiceImpl(
            this.mockClaimRepository,
            this.mockClaimScreenshotRepository,
            this.mockDealService,
            this.mockCampaignSlotRepository,
            this.mockCampaignTypeStepService,
            this.mockStorageService,
            this.mockExtractionService);
  }

  @Test
  void testCreateClaim() {
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_1);
    when(this.mockClaimRepository.existsByOwnerIdAndDealIdAndIsDeletedFalse(OWNER_ID, DEAL_ID))
        .thenReturn(false);
    when(this.mockCampaignSlotRepository.decrementSlotsAvailableIfPositive(SLOT_ID)).thenReturn(1);
    when(this.mockStorageService.store(
            "claims", SCREENSHOT_FILENAME, CONTENT_TYPE, SCREENSHOT_BYTES))
        .thenReturn(SCREENSHOT_KEY);
    final ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
    when(this.mockClaimRepository.save(claimCaptor.capture())).thenReturn(CLAIM_1);

    final Claim result =
        this.claimService.createClaim(
            CLAIM_INPUT, SCREENSHOT_BYTES, SCREENSHOT_FILENAME, CONTENT_TYPE, EXTRACTED_DETAILS);

    assertEquals(CLAIM_1, result);
    final Claim saved = claimCaptor.getValue();
    assertEquals(ORDERED, saved.getStatus());
    assertFalse(saved.getIsDeleted());
    assertEquals(OWNER_ID, saved.getCreatedBy());
    assertEquals(OWNER_ID, saved.getUpdatedBy());

    final ArgumentCaptor<ClaimScreenshot> screenshotCaptor =
        ArgumentCaptor.forClass(ClaimScreenshot.class);
    verify(this.mockClaimScreenshotRepository).save(screenshotCaptor.capture());
    final ClaimScreenshot savedScreenshot = screenshotCaptor.getValue();
    assertEquals(CLAIM_ID, savedScreenshot.getClaimId());
    assertEquals(SCREENSHOT_KEY, savedScreenshot.getStorageKey());
    assertEquals(SCREENSHOT_TYPE_ORDER, savedScreenshot.getType());
    assertEquals(SCREENSHOT_VERIFICATION_STATUS_PENDING, savedScreenshot.getVerificationStatus());
    assertEquals(EXTRACTED_DETAILS, savedScreenshot.getExtractedDetails());
    assertFalse(savedScreenshot.isDeleted());
    assertEquals(OWNER_ID, savedScreenshot.getCreatedBy());
    assertEquals(OWNER_ID, savedScreenshot.getUpdatedBy());
  }

  @Test
  void testCreateClaimWhenAlreadyClaimed() {
    when(this.mockClaimRepository.existsByOwnerIdAndDealIdAndIsDeletedFalse(OWNER_ID, DEAL_ID))
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
                    EXTRACTED_DETAILS));
    assertEquals("You have already claimed this deal", ex.getMessage());
  }

  @Test
  void testSubmitRating() {
    when(this.mockClaimRepository.findByIdAndOwnerIdAndIsDeletedFalse(CLAIM_ID, OWNER_ID))
        .thenReturn(Optional.of(CLAIM_1));
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_1);
    final var steps = stepConfig();
    when(this.mockCampaignTypeStepService.getStepConfig()).thenReturn(steps);
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
    when(this.mockClaimRepository.findByIdAndOwnerIdAndIsDeletedFalse(CLAIM_ID, OWNER_ID))
        .thenReturn(Optional.of(CLAIM_2));
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_1);
    final var steps = stepConfig();
    when(this.mockCampaignTypeStepService.getStepConfig()).thenReturn(steps);

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () ->
                this.claimService.submitRating(
                    CLAIM_ID, OWNER_ID, SCREENSHOT_BYTES, SCREENSHOT_FILENAME, CONTENT_TYPE));
    assertEquals("Rating can only be submitted after Order & Upload", ex.getMessage());
  }

  @Test
  void testSubmitRatingWhenNotFound() {
    when(this.mockClaimRepository.findByIdAndOwnerIdAndIsDeletedFalse(CLAIM_ID, OWNER_ID))
        .thenReturn(Optional.empty());

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
    when(this.mockClaimRepository.findByIdAndOwnerIdAndIsDeletedFalse(CLAIM_ID, OWNER_ID))
        .thenReturn(Optional.of(CLAIM_2));
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_1);
    final var steps = stepConfig();
    when(this.mockCampaignTypeStepService.getStepConfig()).thenReturn(steps);
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
    when(this.mockClaimRepository.findByIdAndOwnerIdAndIsDeletedFalse(CLAIM_ID, OWNER_ID))
        .thenReturn(Optional.of(CLAIM_1));
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_1);
    final var steps = stepConfig();
    when(this.mockCampaignTypeStepService.getStepConfig()).thenReturn(steps);

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
    when(this.mockClaimRepository.findByIdAndOwnerIdAndIsDeletedFalse(CLAIM_ID, OWNER_ID))
        .thenReturn(Optional.empty());

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
    when(this.mockClaimRepository.findByIdAndOwnerIdAndIsDeletedFalse(CLAIM_ID, OWNER_ID))
        .thenReturn(Optional.of(CLAIM_3));
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_1);
    final var steps = stepConfig();
    when(this.mockCampaignTypeStepService.getStepConfig()).thenReturn(steps);
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
    when(this.mockClaimRepository.findByIdAndOwnerIdAndIsDeletedFalse(CLAIM_ID, OWNER_ID))
        .thenReturn(Optional.of(CLAIM_2));
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_1);
    final var steps = stepConfig();
    when(this.mockCampaignTypeStepService.getStepConfig()).thenReturn(steps);

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
    when(this.mockClaimRepository.findByIdAndOwnerIdAndIsDeletedFalse(CLAIM_ID, OWNER_ID))
        .thenReturn(Optional.empty());

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
  void testGetById() {
    when(this.mockClaimRepository.findByIdAndOwnerIdAndIsDeletedFalse(CLAIM_ID, OWNER_ID))
        .thenReturn(Optional.of(CLAIM_1));

    final Claim result = this.claimService.getById(CLAIM_ID, OWNER_ID);

    assertEquals(CLAIM_1, result);
  }

  @Test
  void testGetByIdWhenNotFound() {
    when(this.mockClaimRepository.findByIdAndOwnerIdAndIsDeletedFalse(CLAIM_ID, OWNER_ID))
        .thenReturn(Optional.empty());

    final NotFoundException ex =
        assertThrows(NotFoundException.class, () -> this.claimService.getById(CLAIM_ID, OWNER_ID));
    assertEquals("Claim not found: " + CLAIM_ID, ex.getMessage());
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
    when(this.mockClaimRepository.findByOwnerIdAndIsDeletedFalse(OWNER_ID))
        .thenReturn(List.of(CLAIM_1, CLAIM_2));

    final List<Claim> result = this.claimService.listByOwner(OWNER_ID);

    assertEquals(List.of(CLAIM_1, CLAIM_2), result);
  }

  @Test
  void testListScreenshots() {
    when(this.mockClaimScreenshotRepository.findByClaimIdAndIsDeletedFalseOrderByCreatedAtAsc(
            CLAIM_ID))
        .thenReturn(List.of(SCREENSHOT_1));

    final List<ClaimScreenshot> result = this.claimService.listScreenshots(CLAIM_ID);

    assertEquals(List.of(SCREENSHOT_1), result);
  }

  private Map<com.coddicted.buzzma.campaign.entity.CampaignType, List<CampaignTypeStep>>
      stepConfig() {
    return Map.of(
        CAMPAIGN_TYPE,
        List.of(
            mockStep(CampaignStepType.ORDER, 1),
            mockStep(CampaignStepType.RATING, 2),
            mockStep(CampaignStepType.REVIEW, 3),
            mockStep(CampaignStepType.RETURN_WINDOW, 4)));
  }

  private CampaignTypeStep mockStep(final CampaignStepType type, final int order) {
    final CampaignTypeStep step = mock(CampaignTypeStep.class);
    lenient().when(step.getId()).thenReturn(new CampaignTypeStepId(CAMPAIGN_TYPE, type));
    when(step.getStepOrder()).thenReturn(order);
    return step;
  }
}
