package com.coddicted.buzzma.claim.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.model.CampaignSummary;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.claim.entity.ClaimStatus;
import com.coddicted.buzzma.claim.model.ClaimReviewModel;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.UserRole;
import java.util.Collection;
import java.util.List;
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
  @Captor ArgumentCaptor<Collection<UUID>> campaignIdsCaptor;
  @Captor ArgumentCaptor<Collection<UUID>> mediatorIdsCaptor;
  @Captor ArgumentCaptor<Collection<ClaimStatus>> claimStatusesCaptor;

  private ClaimReviewServiceImpl claimReviewService;

  @BeforeEach
  void setUp() {
    this.claimReviewService =
        new ClaimReviewServiceImpl(this.mockClaimService, this.mockCampaignService);
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
            eq(MEDIATOR_ID), isNull(), isNull(), pageableCaptor.capture()))
        .thenReturn(expected);

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(mediator, null, null, null, requested);

    assertSame(expected, result);
    final Pageable usedPageable = pageableCaptor.getValue();
    assertEquals(1, usedPageable.getPageNumber());
    assertEquals(20, usedPageable.getPageSize());
    assertTrue(usedPageable.getSort().isUnsorted());
    verifyNoInteractions(this.mockCampaignService);
  }

  @Test
  void testGetClaimReviewsForMediatorPassesThroughCampaignIdsFilter() {
    final BuzzmaUser mediator =
        BuzzmaUser.builder().id(MEDIATOR_ID).role(UserRole.ROLE_MEDIATOR).build();
    final Pageable requested = Pageable.ofSize(10);
    final Page<ClaimReviewModel> expected =
        new PageImpl<>(List.of(ClaimReviewModel.builder().build()));
    when(this.mockClaimService.findClaimsToReviewForMediator(
            eq(MEDIATOR_ID), eq(Set.of(OWNED_CAMPAIGN_ID)), isNull(), any()))
        .thenReturn(expected);

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(
            mediator, Set.of(OWNED_CAMPAIGN_ID), null, null, requested);

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
            eq(MEDIATOR_ID), isNull(), eq(Set.of(ClaimStatus.UNDER_REVIEW)), any()))
        .thenReturn(expected);

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(
            mediator, null, null, Set.of(ClaimStatus.UNDER_REVIEW), requested);

    assertSame(expected, result);
  }

  @Test
  void testGetClaimReviewsForMediatorIgnoresMediatorIdsFilter() {
    final BuzzmaUser mediator =
        BuzzmaUser.builder().id(MEDIATOR_ID).role(UserRole.ROLE_MEDIATOR).build();
    final Pageable requested = Pageable.ofSize(10);
    final Page<ClaimReviewModel> expected =
        new PageImpl<>(List.of(ClaimReviewModel.builder().build()));
    when(this.mockClaimService.findClaimsToReviewForMediator(
            eq(MEDIATOR_ID), isNull(), isNull(), any()))
        .thenReturn(expected);

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(
            mediator, null, Set.of(OTHER_MEDIATOR_ID), null, requested);

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
            campaignIdsCaptor.capture(), isNull(), isNull(), any()))
        .thenReturn(expected);

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(agency, null, null, null, requested);

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
            campaignIdsCaptor.capture(), isNull(), isNull(), any()))
        .thenReturn(expected);

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(
            agency, Set.of(OWNED_CAMPAIGN_ID, NOT_OWNED_CAMPAIGN_ID), null, null, requested);

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
            any(), mediatorIdsCaptor.capture(), isNull(), any()))
        .thenReturn(expected);

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(agency, null, Set.of(MEDIATOR_ID), null, requested);

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
            any(), isNull(), claimStatusesCaptor.capture(), any()))
        .thenReturn(expected);

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(
            agency, null, null, Set.of(ClaimStatus.UNDER_REVIEW), requested);

    assertSame(expected, result);
    assertEquals(Set.of(ClaimStatus.UNDER_REVIEW), Set.copyOf(claimStatusesCaptor.getValue()));
  }

  @Test
  void testGetClaimReviewsForAgencyWithNoApplicableCampaignsReturnsEmptyPage() {
    final BuzzmaUser agency = BuzzmaUser.builder().id(AGENCY_ID).role(UserRole.ROLE_AGENCY).build();
    final Pageable requested = Pageable.ofSize(10);

    when(this.mockCampaignService.getByOwnerId(AGENCY_ID)).thenReturn(List.of());

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(agency, null, null, null, requested);

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
            agency, Set.of(NOT_OWNED_CAMPAIGN_ID), null, null, requested);

    assertTrue(result.getContent().isEmpty());
    verifyNoInteractions(this.mockClaimService);
  }
}
