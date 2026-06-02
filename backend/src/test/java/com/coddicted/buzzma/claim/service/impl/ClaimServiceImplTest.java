package com.coddicted.buzzma.claim.service.impl;

import static com.coddicted.buzzma.claim.entity.ClaimStatus.ORDERED;
import static com.coddicted.buzzma.claim.entity.ClaimStatus.PROOF_SUBMITTED;
import static com.coddicted.buzzma.claim.entity.ClaimStatus.UNDER_REVIEW;
import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_ORDER;
import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_RETURN;
import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_REVIEW;
import static com.coddicted.buzzma.claim.entity.ScreenshotVerificationStatus.SCREENSHOT_VERIFICATION_STATUS_PENDING;
import static com.coddicted.buzzma.claim.service.impl.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.campaign.persistence.CampaignSlotRepository;
import com.coddicted.buzzma.campaign.service.DealService;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.persistence.ClaimRepository;
import com.coddicted.buzzma.claim.persistence.ClaimScreenshotRepository;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.exception.ForbiddenException;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import com.coddicted.buzzma.storage.service.StorageService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClaimServiceImplTest {

  @Mock private ClaimRepository mockClaimRepository;
  @Mock private ClaimScreenshotRepository mockClaimScreenshotRepository;
  @Mock private DealService mockDealService;
  @Mock private CampaignSlotRepository mockCampaignSlotRepository;
  @Mock private StorageService mockStorageService;
  private ClaimServiceImpl claimService;

  @BeforeEach
  void setUp() {
    this.claimService =
        new ClaimServiceImpl(
            this.mockClaimRepository,
            this.mockClaimScreenshotRepository,
            this.mockDealService,
            this.mockCampaignSlotRepository,
            this.mockStorageService);
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
  void testSubmitReview() {
    when(this.mockClaimRepository.findByIdAndIsDeletedFalse(CLAIM_ID))
        .thenReturn(Optional.of(CLAIM_1));
    when(this.mockStorageService.store(
            "claims", SCREENSHOT_FILENAME, CONTENT_TYPE, SCREENSHOT_BYTES))
        .thenReturn(SCREENSHOT_KEY);
    final ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
    when(this.mockClaimRepository.save(claimCaptor.capture())).thenReturn(CLAIM_2);

    final Claim result =
        this.claimService.submitReview(
            CLAIM_ID, OWNER_ID, REVIEW_URL, SCREENSHOT_BYTES, SCREENSHOT_FILENAME, CONTENT_TYPE);

    assertEquals(CLAIM_2, result);
    final Claim saved = claimCaptor.getValue();
    assertEquals(PROOF_SUBMITTED, saved.getStatus());
    assertEquals(REVIEW_URL, saved.getReviewUrl());
    assertEquals(OWNER_ID, saved.getUpdatedBy());

    final ArgumentCaptor<ClaimScreenshot> screenshotCaptor =
        ArgumentCaptor.forClass(ClaimScreenshot.class);
    verify(this.mockClaimScreenshotRepository).save(screenshotCaptor.capture());
    assertEquals(CLAIM_ID, screenshotCaptor.getValue().getClaimId());
    assertEquals(SCREENSHOT_TYPE_REVIEW, screenshotCaptor.getValue().getType());
  }

  @Test
  void testSubmitReviewWhenWrongStatus() {
    when(this.mockClaimRepository.findByIdAndIsDeletedFalse(CLAIM_ID))
        .thenReturn(Optional.of(CLAIM_2));

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
    assertEquals("Review can only be submitted when claim is in ORDERED status", ex.getMessage());
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
    when(this.mockClaimRepository.findByIdAndIsDeletedFalse(CLAIM_ID))
        .thenReturn(Optional.of(CLAIM_1));

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
        .thenReturn(Optional.of(CLAIM_2));
    when(this.mockStorageService.store(
            "claims", SCREENSHOT_FILENAME, CONTENT_TYPE, SCREENSHOT_BYTES))
        .thenReturn(SCREENSHOT_KEY);
    final ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
    when(this.mockClaimRepository.save(claimCaptor.capture())).thenReturn(CLAIM_1);

    final Claim result =
        this.claimService.submitReturn(
            CLAIM_ID, OWNER_ID, SCREENSHOT_BYTES, SCREENSHOT_FILENAME, CONTENT_TYPE);

    assertEquals(CLAIM_1, result);
    final Claim saved = claimCaptor.getValue();
    assertEquals(UNDER_REVIEW, saved.getStatus());
    assertEquals(OWNER_ID, saved.getUpdatedBy());

    final ArgumentCaptor<ClaimScreenshot> screenshotCaptor =
        ArgumentCaptor.forClass(ClaimScreenshot.class);
    verify(this.mockClaimScreenshotRepository).save(screenshotCaptor.capture());
    assertEquals(CLAIM_ID, screenshotCaptor.getValue().getClaimId());
    assertEquals(SCREENSHOT_TYPE_RETURN, screenshotCaptor.getValue().getType());
  }

  @Test
  void testSubmitReturnWhenWrongStatus() {
    when(this.mockClaimRepository.findByIdAndIsDeletedFalse(CLAIM_ID))
        .thenReturn(Optional.of(CLAIM_1));

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () ->
                this.claimService.submitReturn(
                    CLAIM_ID, OWNER_ID, SCREENSHOT_BYTES, SCREENSHOT_FILENAME, CONTENT_TYPE));
    assertEquals(
        "Return screenshot can only be submitted when claim is in PROOF_SUBMITTED status",
        ex.getMessage());
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
    when(this.mockClaimRepository.findByIdAndIsDeletedFalse(CLAIM_ID))
        .thenReturn(Optional.of(CLAIM_2));

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
  @Disabled
  // Todo: fix test case
  void testGetByIdWhenNotOwner() {
    when(this.mockClaimRepository.findByIdAndIsDeletedFalse(CLAIM_ID))
        .thenReturn(Optional.of(CLAIM_1));

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
}
