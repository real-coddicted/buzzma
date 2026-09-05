package com.coddicted.buzzma.claim.service.impl;

import static com.coddicted.buzzma.claim.entity.ScreenshotType.SCREENSHOT_TYPE_ORDER;
import static com.coddicted.buzzma.claim.entity.ScreenshotVerificationStatus.SCREENSHOT_VERIFICATION_STATUS_REJECTED;
import static com.coddicted.buzzma.claim.entity.ScreenshotVerificationStatus.SCREENSHOT_VERIFICATION_STATUS_VERIFIED;
import static com.coddicted.buzzma.claim.service.impl.Fixtures.AMOUNT_APPROVED_PAISE;
import static com.coddicted.buzzma.claim.service.impl.Fixtures.CLAIM_1;
import static com.coddicted.buzzma.claim.service.impl.Fixtures.CLAIM_ID;
import static com.coddicted.buzzma.claim.service.impl.Fixtures.DEAL_1;
import static com.coddicted.buzzma.claim.service.impl.Fixtures.DEAL_ID;
import static com.coddicted.buzzma.claim.service.impl.Fixtures.OWNER_ID;
import static com.coddicted.buzzma.claim.service.impl.Fixtures.REVIEWER_COMMENTS;
import static com.coddicted.buzzma.claim.service.impl.Fixtures.SCREENSHOT_1;
import static com.coddicted.buzzma.claim.service.impl.Fixtures.SCREENSHOT_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignShare;
import com.coddicted.buzzma.campaign.model.CampaignSummary;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.campaign.service.CampaignShareService;
import com.coddicted.buzzma.campaign.service.DealService;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.entity.ClaimStatus;
import com.coddicted.buzzma.claim.entity.ReviewerDecision;
import com.coddicted.buzzma.claim.model.ClaimReviewModel;
import com.coddicted.buzzma.claim.model.ClaimWithDeal;
import com.coddicted.buzzma.claim.notification.ClaimReviewEventPublisher;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.shared.enums.Platform;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class ClaimReviewServiceImplTest {

  private static final UUID MEDIATOR_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID OTHER_MEDIATOR_ID =
      UUID.fromString("33333333-3333-3333-3333-333333333333");
  private static final UUID AGENCY_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID OWNED_CAMPAIGN_ID =
      UUID.fromString("44444444-4444-4444-4444-444444444444");
  private static final UUID OTHER_OWNED_CAMPAIGN_ID =
      UUID.fromString("55555555-5555-5555-5555-555555555555");
  private static final UUID NOT_OWNED_CAMPAIGN_ID =
      UUID.fromString("66666666-6666-6666-6666-666666666666");
  private static final UUID BRAND_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");
  private static final UUID SHARED_CAMPAIGN_ID =
      UUID.fromString("88888888-8888-8888-8888-888888888888");

  @Mock private ClaimService mockClaimService;
  @Mock private CampaignService mockCampaignService;
  @Mock private CampaignShareService mockCampaignShareService;
  @Mock private DealService mockDealService;
  @Mock private ClaimReviewEventPublisher mockClaimReviewEventPublisher;
  @Captor ArgumentCaptor<Collection<UUID>> campaignIdsCaptor;
  @Captor ArgumentCaptor<Collection<UUID>> mediatorIdsCaptor;
  @Captor ArgumentCaptor<Collection<ClaimStatus>> claimStatusesCaptor;
  @Captor ArgumentCaptor<Collection<String>> brandsCaptor;
  @Captor ArgumentCaptor<Collection<Platform>> platformsCaptor;

  private ClaimReviewServiceImpl claimReviewService;

  @BeforeEach
  void setUp() {
    this.claimReviewService =
        new ClaimReviewServiceImpl(
            this.mockClaimService,
            this.mockCampaignService,
            this.mockCampaignShareService,
            this.mockDealService,
            this.mockClaimReviewEventPublisher);
  }

  @Test
  void testGetClaimReviewsForMediatorScopesByOwnDealsOnlyWhenNoFiltersGiven() {
    final BuzzmaUser mediator =
        BuzzmaUser.builder().id(MEDIATOR_ID).role(UserRole.ROLE_MEDIATOR).build();
    final Pageable requested = PageRequest.of(1, 20, Sort.by("createdAt"));
    final Page<ClaimReviewModel> expected =
        new PageImpl<>(List.of(ClaimReviewModel.builder().build()));

    final ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    when(this.mockClaimService.findClaimsToReviewForMediator(
            eq(MEDIATOR_ID), isNull(), isNull(), isNull(), isNull(), pageableCaptor.capture()))
        .thenReturn(expected);

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(mediator, null, null, null, null, null, requested);

    assertSame(expected, result);
    final Pageable usedPageable = pageableCaptor.getValue();
    assertEquals(1, usedPageable.getPageNumber());
    assertEquals(20, usedPageable.getPageSize());
    assertTrue(usedPageable.getSort().isUnsorted());
    verifyNoInteractions(this.mockCampaignService);
  }

  @Test
  void testGetClaimReviewsForMediatorWithUnpagedPageableDoesNotThrow() {
    final BuzzmaUser mediator =
        BuzzmaUser.builder().id(MEDIATOR_ID).role(UserRole.ROLE_MEDIATOR).build();
    final Page<ClaimReviewModel> expected =
        new PageImpl<>(List.of(ClaimReviewModel.builder().build()));

    final ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    when(this.mockClaimService.findClaimsToReviewForMediator(
            eq(MEDIATOR_ID), isNull(), isNull(), isNull(), isNull(), pageableCaptor.capture()))
        .thenReturn(expected);

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(
            mediator, null, null, null, null, null, Pageable.unpaged());

    assertSame(expected, result);
    assertTrue(pageableCaptor.getValue().isUnpaged());
  }

  @Test
  void testGetClaimReviewsForMediatorPassesThroughCampaignIdsFilter() {
    final BuzzmaUser mediator =
        BuzzmaUser.builder().id(MEDIATOR_ID).role(UserRole.ROLE_MEDIATOR).build();
    final Pageable requested = Pageable.ofSize(10);
    final Page<ClaimReviewModel> expected =
        new PageImpl<>(List.of(ClaimReviewModel.builder().build()));
    when(this.mockClaimService.findClaimsToReviewForMediator(
            eq(MEDIATOR_ID), eq(Set.of(OWNED_CAMPAIGN_ID)), isNull(), isNull(), isNull(), any()))
        .thenReturn(expected);

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(
            mediator, Set.of(OWNED_CAMPAIGN_ID), null, null, null, null, requested);

    assertSame(expected, result);
  }

  @Test
  void testGetClaimReviewsForMediatorPassesThroughClaimStatusFilter() {
    final BuzzmaUser mediator =
        BuzzmaUser.builder().id(MEDIATOR_ID).role(UserRole.ROLE_MEDIATOR).build();
    final Pageable requested = Pageable.ofSize(10);
    final Page<ClaimReviewModel> expected =
        new PageImpl<>(List.of(ClaimReviewModel.builder().build()));
    when(this.mockClaimService.findClaimsToReviewForMediator(
            eq(MEDIATOR_ID),
            isNull(),
            eq(Set.of(ClaimStatus.UNDER_REVIEW)),
            isNull(),
            isNull(),
            any()))
        .thenReturn(expected);

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(
            mediator, null, null, Set.of(ClaimStatus.UNDER_REVIEW), null, null, requested);

    assertSame(expected, result);
  }

  @Test
  void testGetClaimReviewsForMediatorPassesThroughBrandAndPlatformFilters() {
    final BuzzmaUser mediator =
        BuzzmaUser.builder().id(MEDIATOR_ID).role(UserRole.ROLE_MEDIATOR).build();
    final Pageable requested = Pageable.ofSize(10);
    final Page<ClaimReviewModel> expected =
        new PageImpl<>(List.of(ClaimReviewModel.builder().build()));
    when(this.mockClaimService.findClaimsToReviewForMediator(
            eq(MEDIATOR_ID),
            isNull(),
            isNull(),
            brandsCaptor.capture(),
            platformsCaptor.capture(),
            any()))
        .thenReturn(expected);

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(
            mediator,
            null,
            null,
            null,
            Set.of("Nike"),
            Set.of(Platform.PLATFORM_AMAZON),
            requested);

    assertSame(expected, result);
    assertEquals(Set.of("Nike"), Set.copyOf(brandsCaptor.getValue()));
    assertEquals(Set.of(Platform.PLATFORM_AMAZON), Set.copyOf(platformsCaptor.getValue()));
  }

  @Test
  void testGetClaimReviewsForMediatorIgnoresMediatorIdsFilter() {
    final BuzzmaUser mediator =
        BuzzmaUser.builder().id(MEDIATOR_ID).role(UserRole.ROLE_MEDIATOR).build();
    final Pageable requested = Pageable.ofSize(10);
    final Page<ClaimReviewModel> expected =
        new PageImpl<>(List.of(ClaimReviewModel.builder().build()));
    when(this.mockClaimService.findClaimsToReviewForMediator(
            eq(MEDIATOR_ID), isNull(), isNull(), isNull(), isNull(), any()))
        .thenReturn(expected);

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(
            mediator, null, Set.of(OTHER_MEDIATOR_ID), null, null, null, requested);

    assertSame(expected, result);
    verifyNoInteractions(this.mockCampaignService);
  }

  @Test
  void testGetClaimReviewsForAgencyUsesOwnedCampaignsWhenNoFiltersGiven() {
    final BuzzmaUser agency = BuzzmaUser.builder().id(AGENCY_ID).role(UserRole.ROLE_AGENCY).build();
    final Pageable requested = Pageable.ofSize(10);

    final Campaign ownedCampaign = Campaign.builder().id(OWNED_CAMPAIGN_ID).build();
    when(this.mockCampaignService.getByOwnerId(AGENCY_ID))
        .thenReturn(List.of(CampaignSummary.builder().campaign(ownedCampaign).build()));

    final Page<ClaimReviewModel> expected =
        new PageImpl<>(List.of(ClaimReviewModel.builder().build()));
    when(this.mockClaimService.findClaimsToReviewForCampaigns(
            campaignIdsCaptor.capture(), isNull(), isNull(), isNull(), isNull(), any()))
        .thenReturn(expected);

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(agency, null, null, null, null, null, requested);

    assertSame(expected, result);
    assertEquals(Set.of(OWNED_CAMPAIGN_ID), Set.copyOf(campaignIdsCaptor.getValue()));
  }

  @Test
  void testGetClaimReviewsForBrandIncludesSharedCampaigns() {
    final BuzzmaUser brand = BuzzmaUser.builder().id(BRAND_ID).role(UserRole.ROLE_BRAND).build();
    final Pageable requested = Pageable.ofSize(10);

    when(this.mockCampaignService.getByOwnerId(BRAND_ID)).thenReturn(List.of());
    when(this.mockCampaignShareService.findByToUserId(BRAND_ID))
        .thenReturn(List.of(CampaignShare.builder().campaignId(SHARED_CAMPAIGN_ID).build()));

    final Page<ClaimReviewModel> expected =
        new PageImpl<>(List.of(ClaimReviewModel.builder().build()));
    when(this.mockClaimService.findClaimsToReviewForCampaigns(
            campaignIdsCaptor.capture(), isNull(), isNull(), isNull(), isNull(), any()))
        .thenReturn(expected);

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(brand, null, null, null, null, null, requested);

    assertSame(expected, result);
    assertEquals(Set.of(SHARED_CAMPAIGN_ID), Set.copyOf(campaignIdsCaptor.getValue()));
  }

  @Test
  void testGetClaimReviewsForAgencyCampaignIdsFilterIntersectsWithOwnedCampaigns() {
    final BuzzmaUser agency = BuzzmaUser.builder().id(AGENCY_ID).role(UserRole.ROLE_AGENCY).build();
    final Pageable requested = Pageable.ofSize(10);

    when(this.mockCampaignService.getByOwnerId(AGENCY_ID))
        .thenReturn(
            List.of(
                CampaignSummary.builder()
                    .campaign(Campaign.builder().id(OWNED_CAMPAIGN_ID).build())
                    .build(),
                CampaignSummary.builder()
                    .campaign(Campaign.builder().id(OTHER_OWNED_CAMPAIGN_ID).build())
                    .build()));

    final Page<ClaimReviewModel> expected =
        new PageImpl<>(List.of(ClaimReviewModel.builder().build()));
    when(this.mockClaimService.findClaimsToReviewForCampaigns(
            campaignIdsCaptor.capture(), isNull(), isNull(), isNull(), isNull(), any()))
        .thenReturn(expected);

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(
            agency,
            Set.of(OWNED_CAMPAIGN_ID, NOT_OWNED_CAMPAIGN_ID),
            null,
            null,
            null,
            null,
            requested);

    assertSame(expected, result);
    assertEquals(Set.of(OWNED_CAMPAIGN_ID), Set.copyOf(campaignIdsCaptor.getValue()));
  }

  @Test
  void testGetClaimReviewsForAgencyMediatorIdsFilterPassesThrough() {
    final BuzzmaUser agency = BuzzmaUser.builder().id(AGENCY_ID).role(UserRole.ROLE_AGENCY).build();
    final Pageable requested = Pageable.ofSize(10);

    when(this.mockCampaignService.getByOwnerId(AGENCY_ID))
        .thenReturn(
            List.of(
                CampaignSummary.builder()
                    .campaign(Campaign.builder().id(OWNED_CAMPAIGN_ID).build())
                    .build()));

    final Page<ClaimReviewModel> expected =
        new PageImpl<>(List.of(ClaimReviewModel.builder().build()));
    when(this.mockClaimService.findClaimsToReviewForCampaigns(
            any(), mediatorIdsCaptor.capture(), isNull(), isNull(), isNull(), any()))
        .thenReturn(expected);

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(
            agency, null, Set.of(MEDIATOR_ID), null, null, null, requested);

    assertSame(expected, result);
    assertEquals(Set.of(MEDIATOR_ID), Set.copyOf(mediatorIdsCaptor.getValue()));
  }

  @Test
  void testGetClaimReviewsForAgencyClaimStatusFilterPassesThrough() {
    final BuzzmaUser agency = BuzzmaUser.builder().id(AGENCY_ID).role(UserRole.ROLE_AGENCY).build();
    final Pageable requested = Pageable.ofSize(10);

    when(this.mockCampaignService.getByOwnerId(AGENCY_ID))
        .thenReturn(
            List.of(
                CampaignSummary.builder()
                    .campaign(Campaign.builder().id(OWNED_CAMPAIGN_ID).build())
                    .build()));

    final Page<ClaimReviewModel> expected =
        new PageImpl<>(List.of(ClaimReviewModel.builder().build()));
    when(this.mockClaimService.findClaimsToReviewForCampaigns(
            any(), isNull(), claimStatusesCaptor.capture(), isNull(), isNull(), any()))
        .thenReturn(expected);

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(
            agency, null, null, Set.of(ClaimStatus.UNDER_REVIEW), null, null, requested);

    assertSame(expected, result);
    assertEquals(Set.of(ClaimStatus.UNDER_REVIEW), Set.copyOf(claimStatusesCaptor.getValue()));
  }

  @Test
  void testGetClaimReviewsForAgencyPassesThroughBrandAndPlatformFilters() {
    final BuzzmaUser agency = BuzzmaUser.builder().id(AGENCY_ID).role(UserRole.ROLE_AGENCY).build();
    final Pageable requested = Pageable.ofSize(10);

    when(this.mockCampaignService.getByOwnerId(AGENCY_ID))
        .thenReturn(
            List.of(
                CampaignSummary.builder()
                    .campaign(Campaign.builder().id(OWNED_CAMPAIGN_ID).build())
                    .build()));

    final Page<ClaimReviewModel> expected =
        new PageImpl<>(List.of(ClaimReviewModel.builder().build()));
    when(this.mockClaimService.findClaimsToReviewForCampaigns(
            any(), isNull(), isNull(), brandsCaptor.capture(), platformsCaptor.capture(), any()))
        .thenReturn(expected);

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(
            agency,
            null,
            null,
            null,
            Set.of("Adidas"),
            Set.of(Platform.PLATFORM_FLIPKART),
            requested);

    assertSame(expected, result);
    assertEquals(Set.of("Adidas"), Set.copyOf(brandsCaptor.getValue()));
    assertEquals(Set.of(Platform.PLATFORM_FLIPKART), Set.copyOf(platformsCaptor.getValue()));
  }

  @Test
  void testGetClaimReviewsForAgencyWithNoApplicableCampaignsReturnsEmptyPage() {
    final BuzzmaUser agency = BuzzmaUser.builder().id(AGENCY_ID).role(UserRole.ROLE_AGENCY).build();
    final Pageable requested = Pageable.ofSize(10);

    when(this.mockCampaignService.getByOwnerId(AGENCY_ID)).thenReturn(List.of());

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(agency, null, null, null, null, null, requested);

    assertTrue(result.getContent().isEmpty());
    verifyNoInteractions(this.mockClaimService);
  }

  @Test
  void testGetClaimReviewsForAgencyCampaignIdsFilterOutsideOwnedCampaignsReturnsEmptyPage() {
    final BuzzmaUser agency = BuzzmaUser.builder().id(AGENCY_ID).role(UserRole.ROLE_AGENCY).build();
    final Pageable requested = Pageable.ofSize(10);

    when(this.mockCampaignService.getByOwnerId(AGENCY_ID))
        .thenReturn(
            List.of(
                CampaignSummary.builder()
                    .campaign(Campaign.builder().id(OWNED_CAMPAIGN_ID).build())
                    .build()));

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(
            agency, Set.of(NOT_OWNED_CAMPAIGN_ID), null, null, null, null, requested);

    assertTrue(result.getContent().isEmpty());
    verifyNoInteractions(this.mockClaimService);
  }

  @Test
  void testReviewScreenshotRejected() {
    when(this.mockClaimService.getById(CLAIM_ID, OWNER_ID)).thenReturn(CLAIM_1);
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_1);
    when(this.mockClaimService.getScreenshotById(SCREENSHOT_ID)).thenReturn(SCREENSHOT_1);
    final Claim rejectedClaim =
        CLAIM_1.toBuilder().status(ClaimStatus.PROOF_REJECTED).updatedBy(OWNER_ID).build();
    final ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
    when(this.mockClaimService.save(claimCaptor.capture())).thenReturn(rejectedClaim);

    final ClaimWithDeal result =
        this.claimReviewService.reviewScreenshot(
            SCREENSHOT_ID,
            CLAIM_ID,
            SCREENSHOT_VERIFICATION_STATUS_REJECTED,
            OWNER_ID,
            REVIEWER_COMMENTS);

    assertEquals(rejectedClaim, result.claim());
    assertEquals(DEAL_1, result.deal());
    assertEquals(ClaimStatus.PROOF_REJECTED, claimCaptor.getValue().getStatus());

    final ArgumentCaptor<ClaimScreenshot> screenshotCaptor =
        ArgumentCaptor.forClass(ClaimScreenshot.class);
    verify(this.mockClaimService).saveScreenshot(screenshotCaptor.capture());
    assertEquals(
        SCREENSHOT_VERIFICATION_STATUS_REJECTED,
        screenshotCaptor.getValue().getVerificationStatus());
    assertEquals(REVIEWER_COMMENTS, screenshotCaptor.getValue().getReviewerComments());

    verify(this.mockClaimReviewEventPublisher)
        .publishScreenshotReviewedEvent(
            rejectedClaim,
            DEAL_1,
            SCREENSHOT_TYPE_ORDER,
            SCREENSHOT_VERIFICATION_STATUS_REJECTED,
            OWNER_ID,
            REVIEWER_COMMENTS);
  }

  @Test
  void testReviewScreenshotVerifiedDoesNotPublishEvent() {
    when(this.mockClaimService.getById(CLAIM_ID, OWNER_ID)).thenReturn(CLAIM_1);
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_1);
    when(this.mockClaimService.getScreenshotById(SCREENSHOT_ID)).thenReturn(SCREENSHOT_1);

    final ClaimWithDeal result =
        this.claimReviewService.reviewScreenshot(
            SCREENSHOT_ID, CLAIM_ID, SCREENSHOT_VERIFICATION_STATUS_VERIFIED, OWNER_ID, null);

    assertEquals(CLAIM_1, result.claim());
    assertEquals(DEAL_1, result.deal());
    verifyNoInteractions(this.mockClaimReviewEventPublisher);
  }

  @Test
  void testBulkApproveClaimReviews() {
    when(this.mockClaimService.getById(CLAIM_ID, OWNER_ID)).thenReturn(CLAIM_1);
    when(this.mockClaimService.listScreenshots(CLAIM_ID)).thenReturn(List.of(SCREENSHOT_1));
    final ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
    when(this.mockClaimService.save(claimCaptor.capture())).thenReturn(CLAIM_1);
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_1);

    final List<ClaimWithDeal> results =
        this.claimReviewService.bulkApproveClaimReviews(
            Map.of(CLAIM_ID, AMOUNT_APPROVED_PAISE), OWNER_ID);

    assertEquals(1, results.size());
    assertEquals(DEAL_1, results.get(0).deal());
    final Claim saved = claimCaptor.getValue();
    assertEquals(ClaimStatus.APPROVED, saved.getStatus());
    assertEquals(OWNER_ID, saved.getReviewerId());

    final ArgumentCaptor<ClaimScreenshot> screenshotCaptor =
        ArgumentCaptor.forClass(ClaimScreenshot.class);
    verify(this.mockClaimService).saveScreenshot(screenshotCaptor.capture());
    assertEquals(
        SCREENSHOT_VERIFICATION_STATUS_VERIFIED,
        screenshotCaptor.getValue().getVerificationStatus());

    verify(this.mockClaimReviewEventPublisher)
        .publishClaimDecisionEvent(CLAIM_1, ClaimStatus.APPROVED, null);
  }

  @Test
  void testBulkApproveClaimReviewsWhenClaimNotFound() {
    when(this.mockClaimService.getById(CLAIM_ID, OWNER_ID))
        .thenThrow(new NotFoundException("Claim not found: " + CLAIM_ID));

    final NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () ->
                this.claimReviewService.bulkApproveClaimReviews(
                    Map.of(CLAIM_ID, AMOUNT_APPROVED_PAISE), OWNER_ID));
    assertEquals("Claim not found: " + CLAIM_ID, ex.getMessage());
  }

  @Test
  void testSubmitClaimReviewApprovedPublishesDecisionEvent() {
    when(this.mockClaimService.getById(CLAIM_ID, OWNER_ID)).thenReturn(CLAIM_1);
    when(this.mockClaimService.listScreenshots(CLAIM_ID)).thenReturn(List.of(SCREENSHOT_1));
    final ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
    when(this.mockClaimService.save(claimCaptor.capture())).thenReturn(CLAIM_1);
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_1);

    this.claimReviewService.submitClaimReview(
        CLAIM_ID,
        OWNER_ID,
        UserRole.ROLE_AGENCY,
        ReviewerDecision.APPROVED,
        REVIEWER_COMMENTS,
        AMOUNT_APPROVED_PAISE);

    final Claim saved = claimCaptor.getValue();
    assertEquals(ClaimStatus.APPROVED, saved.getStatus());
    verify(this.mockClaimReviewEventPublisher)
        .publishClaimDecisionEvent(CLAIM_1, ClaimStatus.APPROVED, REVIEWER_COMMENTS);
  }

  @Test
  void testSubmitClaimReviewRejectedPublishesDecisionEvent() {
    when(this.mockClaimService.getById(CLAIM_ID, OWNER_ID)).thenReturn(CLAIM_1);
    final Claim rejectedClaim =
        CLAIM_1.toBuilder().status(ClaimStatus.REJECTED).updatedBy(OWNER_ID).build();
    final ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
    when(this.mockClaimService.save(claimCaptor.capture())).thenReturn(rejectedClaim);
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_1);

    this.claimReviewService.submitClaimReview(
        CLAIM_ID,
        OWNER_ID,
        UserRole.ROLE_AGENCY,
        ReviewerDecision.REJECTED,
        REVIEWER_COMMENTS,
        null);

    assertEquals(ClaimStatus.REJECTED, claimCaptor.getValue().getStatus());
    verify(this.mockClaimReviewEventPublisher)
        .publishClaimDecisionEvent(rejectedClaim, ClaimStatus.REJECTED, REVIEWER_COMMENTS);
  }

  @Test
  void testSubmitClaimReviewVerifiedByMediatorDoesNotPublishDecisionEvent() {
    when(this.mockClaimService.getById(CLAIM_ID, OWNER_ID)).thenReturn(CLAIM_1);
    when(this.mockClaimService.save(any(Claim.class))).thenReturn(CLAIM_1);
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_1);

    this.claimReviewService.submitClaimReview(
        CLAIM_ID, OWNER_ID, UserRole.ROLE_MEDIATOR, ReviewerDecision.VERIFIED, null, null);

    verifyNoInteractions(this.mockClaimReviewEventPublisher);
  }

  @Test
  void testSubmitClaimReviewBrandVerifiedSetsFlagWithoutChangingClaimStatus() {
    when(this.mockClaimService.getById(CLAIM_ID, OWNER_ID)).thenReturn(CLAIM_1);
    final ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
    when(this.mockClaimService.save(claimCaptor.capture())).thenReturn(CLAIM_1);
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_1);

    this.claimReviewService.submitClaimReview(
        CLAIM_ID, OWNER_ID, UserRole.ROLE_BRAND, ReviewerDecision.BRAND_VERIFIED, null, null);

    final Claim saved = claimCaptor.getValue();
    assertEquals(true, saved.getBrandVerified());
    assertEquals(OWNER_ID, saved.getUpdatedBy());
    assertEquals(ClaimStatus.ORDERED, saved.getStatus());
    assertNull(saved.getAmountApprovedPaise());
    assertNull(saved.getReviewerId());
    assertNull(saved.getReviewerComments());
    verifyNoInteractions(this.mockClaimReviewEventPublisher);
    verify(this.mockClaimService).getById(CLAIM_ID, OWNER_ID);
    verify(this.mockClaimService).save(saved);
    verifyNoMoreInteractions(this.mockClaimService);
  }

  @Test
  void testSubmitClaimReviewBrandVerifiedLeavesMediatorVerifiedUntouched() {
    when(this.mockClaimService.getById(CLAIM_ID, OWNER_ID)).thenReturn(CLAIM_1);
    final ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
    when(this.mockClaimService.save(claimCaptor.capture())).thenReturn(CLAIM_1);
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_1);

    this.claimReviewService.submitClaimReview(
        CLAIM_ID, OWNER_ID, UserRole.ROLE_BRAND, ReviewerDecision.BRAND_VERIFIED, null, null);

    assertNull(claimCaptor.getValue().getMediatorVerified());
  }

  @Test
  void testSubmitClaimReviewBrandVerifiedByAgencyThrows() {
    when(this.mockClaimService.getById(CLAIM_ID, OWNER_ID)).thenReturn(CLAIM_1);

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () ->
                this.claimReviewService.submitClaimReview(
                    CLAIM_ID,
                    OWNER_ID,
                    UserRole.ROLE_AGENCY,
                    ReviewerDecision.BRAND_VERIFIED,
                    null,
                    null));
    assertEquals("BRAND_VERIFIED decision is only allowed for BRAND role", ex.getMessage());
  }

  @Test
  void testSubmitClaimReviewApprovedByBrandThrows() {
    when(this.mockClaimService.getById(CLAIM_ID, OWNER_ID)).thenReturn(CLAIM_1);

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () ->
                this.claimReviewService.submitClaimReview(
                    CLAIM_ID,
                    OWNER_ID,
                    UserRole.ROLE_BRAND,
                    ReviewerDecision.APPROVED,
                    REVIEWER_COMMENTS,
                    AMOUNT_APPROVED_PAISE));
    assertEquals("BRAND cannot approve a claim", ex.getMessage());
  }

  @Test
  void testSubmitClaimReviewVerifiedByBrandThrows() {
    when(this.mockClaimService.getById(CLAIM_ID, OWNER_ID)).thenReturn(CLAIM_1);

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () ->
                this.claimReviewService.submitClaimReview(
                    CLAIM_ID,
                    OWNER_ID,
                    UserRole.ROLE_BRAND,
                    ReviewerDecision.VERIFIED,
                    null,
                    null));
    assertEquals("VERIFIED decision is only allowed for MEDIATOR role", ex.getMessage());
  }

  @Test
  void testSubmitClaimReviewRejectedByBrandStoresRemarksAndPublishesDecisionEvent() {
    when(this.mockClaimService.getById(CLAIM_ID, OWNER_ID)).thenReturn(CLAIM_1);
    final Claim rejectedClaim =
        CLAIM_1.toBuilder().status(ClaimStatus.REJECTED).updatedBy(OWNER_ID).build();
    final ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
    when(this.mockClaimService.save(claimCaptor.capture())).thenReturn(rejectedClaim);
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_1);

    this.claimReviewService.submitClaimReview(
        CLAIM_ID,
        OWNER_ID,
        UserRole.ROLE_BRAND,
        ReviewerDecision.REJECTED,
        REVIEWER_COMMENTS,
        null);

    final Claim saved = claimCaptor.getValue();
    assertEquals(ClaimStatus.REJECTED, saved.getStatus());
    assertEquals(REVIEWER_COMMENTS, saved.getReviewerComments());
    assertEquals(OWNER_ID, saved.getReviewerId());
    assertNull(saved.getBrandVerified());
    verify(this.mockClaimReviewEventPublisher)
        .publishClaimDecisionEvent(rejectedClaim, ClaimStatus.REJECTED, REVIEWER_COMMENTS);
  }
}
