package com.coddicted.buzzma.claim.service.impl;

import static com.coddicted.buzzma.claim.entity.ClaimReviewStatus.CLAIM_REVIEW_STATUS_OBJECTED;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.model.CampaignSummary;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.campaign.service.DealService;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimReviewStatus;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.entity.ClaimStatus;
import com.coddicted.buzzma.claim.model.ClaimReviewModel;
import com.coddicted.buzzma.claim.model.ClaimWithDeal;
import com.coddicted.buzzma.claim.notification.ClaimReviewEventPublisher;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.shared.enums.Platform;
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

  @Mock private ClaimService mockClaimService;
  @Mock private CampaignService mockCampaignService;
  @Mock private DealService mockDealService;
  @Mock private ClaimReviewEventPublisher mockClaimReviewEventPublisher;
  @Captor ArgumentCaptor<Collection<UUID>> campaignIdsCaptor;
  @Captor ArgumentCaptor<Collection<UUID>> mediatorIdsCaptor;
  @Captor ArgumentCaptor<Collection<ClaimStatus>> claimStatusesCaptor;
  @Captor ArgumentCaptor<Collection<String>> brandsCaptor;
  @Captor ArgumentCaptor<Collection<Platform>> platformsCaptor;
  @Captor ArgumentCaptor<Collection<ClaimReviewStatus>> reviewStatusesCaptor;

  private ClaimReviewServiceImpl claimReviewService;

  @BeforeEach
  void setUp() {
    this.claimReviewService =
        new ClaimReviewServiceImpl(
            this.mockClaimService,
            this.mockCampaignService,
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
            eq(MEDIATOR_ID),
            isNull(),
            isNull(),
            isNull(),
            isNull(),
            isNull(),
            pageableCaptor.capture()))
        .thenReturn(expected);

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(
            mediator, null, null, null, null, null, null, requested);

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
            eq(MEDIATOR_ID),
            isNull(),
            isNull(),
            isNull(),
            isNull(),
            isNull(),
            pageableCaptor.capture()))
        .thenReturn(expected);

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(
            mediator, null, null, null, null, null, null, Pageable.unpaged());

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
            eq(MEDIATOR_ID),
            eq(Set.of(OWNED_CAMPAIGN_ID)),
            isNull(),
            isNull(),
            isNull(),
            isNull(),
            any()))
        .thenReturn(expected);

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(
            mediator, Set.of(OWNED_CAMPAIGN_ID), null, null, null, null, null, requested);

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
            isNull(),
            any()))
        .thenReturn(expected);

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(
            mediator, null, null, Set.of(ClaimStatus.UNDER_REVIEW), null, null, null, requested);

    assertSame(expected, result);
  }

  @Test
  void testGetClaimReviewsForMediatorPassesThroughBrandPlatformAndReviewStatusFilters() {
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
            reviewStatusesCaptor.capture(),
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
            Set.of(ClaimReviewStatus.CLAIM_REVIEW_STATUS_APPROVED),
            requested);

    assertSame(expected, result);
    assertEquals(Set.of("Nike"), Set.copyOf(brandsCaptor.getValue()));
    assertEquals(Set.of(Platform.PLATFORM_AMAZON), Set.copyOf(platformsCaptor.getValue()));
    assertEquals(
        Set.of(ClaimReviewStatus.CLAIM_REVIEW_STATUS_APPROVED),
        Set.copyOf(reviewStatusesCaptor.getValue()));
  }

  @Test
  void testGetClaimReviewsForMediatorIgnoresMediatorIdsFilter() {
    final BuzzmaUser mediator =
        BuzzmaUser.builder().id(MEDIATOR_ID).role(UserRole.ROLE_MEDIATOR).build();
    final Pageable requested = Pageable.ofSize(10);
    final Page<ClaimReviewModel> expected =
        new PageImpl<>(List.of(ClaimReviewModel.builder().build()));
    when(this.mockClaimService.findClaimsToReviewForMediator(
            eq(MEDIATOR_ID), isNull(), isNull(), isNull(), isNull(), isNull(), any()))
        .thenReturn(expected);

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(
            mediator, null, Set.of(OTHER_MEDIATOR_ID), null, null, null, null, requested);

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
            campaignIdsCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(), any()))
        .thenReturn(expected);

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(
            agency, null, null, null, null, null, null, requested);

    assertSame(expected, result);
    assertEquals(Set.of(OWNED_CAMPAIGN_ID), Set.copyOf(campaignIdsCaptor.getValue()));
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
            campaignIdsCaptor.capture(), isNull(), isNull(), isNull(), isNull(), isNull(), any()))
        .thenReturn(expected);

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(
            agency,
            Set.of(OWNED_CAMPAIGN_ID, NOT_OWNED_CAMPAIGN_ID),
            null,
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
            any(), mediatorIdsCaptor.capture(), isNull(), isNull(), isNull(), isNull(), any()))
        .thenReturn(expected);

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(
            agency, null, Set.of(MEDIATOR_ID), null, null, null, null, requested);

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
            any(), isNull(), claimStatusesCaptor.capture(), isNull(), isNull(), isNull(), any()))
        .thenReturn(expected);

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(
            agency, null, null, Set.of(ClaimStatus.UNDER_REVIEW), null, null, null, requested);

    assertSame(expected, result);
    assertEquals(Set.of(ClaimStatus.UNDER_REVIEW), Set.copyOf(claimStatusesCaptor.getValue()));
  }

  @Test
  void testGetClaimReviewsForAgencyPassesThroughBrandPlatformAndReviewStatusFilters() {
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
            any(),
            isNull(),
            isNull(),
            brandsCaptor.capture(),
            platformsCaptor.capture(),
            reviewStatusesCaptor.capture(),
            any()))
        .thenReturn(expected);

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(
            agency,
            null,
            null,
            null,
            Set.of("Adidas"),
            Set.of(Platform.PLATFORM_FLIPKART),
            Set.of(ClaimReviewStatus.CLAIM_REVIEW_STATUS_REJECTED),
            requested);

    assertSame(expected, result);
    assertEquals(Set.of("Adidas"), Set.copyOf(brandsCaptor.getValue()));
    assertEquals(Set.of(Platform.PLATFORM_FLIPKART), Set.copyOf(platformsCaptor.getValue()));
    assertEquals(
        Set.of(ClaimReviewStatus.CLAIM_REVIEW_STATUS_REJECTED),
        Set.copyOf(reviewStatusesCaptor.getValue()));
  }

  @Test
  void testGetClaimReviewsForAgencyWithNoApplicableCampaignsReturnsEmptyPage() {
    final BuzzmaUser agency = BuzzmaUser.builder().id(AGENCY_ID).role(UserRole.ROLE_AGENCY).build();
    final Pageable requested = Pageable.ofSize(10);

    when(this.mockCampaignService.getByOwnerId(AGENCY_ID)).thenReturn(List.of());

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(
            agency, null, null, null, null, null, null, requested);

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
            agency, Set.of(NOT_OWNED_CAMPAIGN_ID), null, null, null, null, null, requested);

    assertTrue(result.getContent().isEmpty());
    verifyNoInteractions(this.mockClaimService);
  }

  @Test
  void testReviewScreenshotRejected() {
    when(this.mockClaimService.getById(CLAIM_ID, OWNER_ID)).thenReturn(CLAIM_1);
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_1);
    when(this.mockClaimService.getScreenshotById(SCREENSHOT_ID)).thenReturn(SCREENSHOT_1);
    final Claim rejectedClaim =
        CLAIM_1.toBuilder()
            .status(ClaimStatus.PROOF_REJECTED)
            .reviewStatus(CLAIM_REVIEW_STATUS_OBJECTED)
            .updatedBy(OWNER_ID)
            .build();
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
    assertEquals(CLAIM_REVIEW_STATUS_OBJECTED, claimCaptor.getValue().getReviewStatus());

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
    assertEquals(ClaimReviewStatus.CLAIM_REVIEW_STATUS_APPROVED, saved.getReviewStatus());
    assertEquals(OWNER_ID, saved.getReviewerId());

    final ArgumentCaptor<ClaimScreenshot> screenshotCaptor =
        ArgumentCaptor.forClass(ClaimScreenshot.class);
    verify(this.mockClaimService).saveScreenshot(screenshotCaptor.capture());
    assertEquals(
        SCREENSHOT_VERIFICATION_STATUS_VERIFIED,
        screenshotCaptor.getValue().getVerificationStatus());
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
}
