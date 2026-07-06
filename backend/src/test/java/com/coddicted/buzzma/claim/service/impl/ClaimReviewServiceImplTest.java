package com.coddicted.buzzma.claim.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.model.CampaignSummary;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.claim.model.ClaimReviewModel;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.UserRole;
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
  private static final UUID AGENCY_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID OWNED_CAMPAIGN_ID =
      UUID.fromString("44444444-4444-4444-4444-444444444444");

  @Mock private ClaimService mockClaimService;
  @Mock private CampaignService mockCampaignService;
  @Captor ArgumentCaptor<List<UUID>> campaignIdsCaptor;

  private ClaimReviewServiceImpl claimReviewService;

  @BeforeEach
  void setUp() {
    this.claimReviewService =
        new ClaimReviewServiceImpl(this.mockClaimService, this.mockCampaignService);
  }

  @Test
  void testGetClaimReviewsForMediatorScopesByOwnDealsOnly() {
    final BuzzmaUser mediator =
        BuzzmaUser.builder().id(MEDIATOR_ID).role(UserRole.ROLE_MEDIATOR).build();
    final Pageable requested = PageRequest.of(1, 20, Sort.by("createdAt"));
    final Page<ClaimReviewModel> expected =
        new PageImpl<>(List.of(ClaimReviewModel.builder().build()));

    final ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    when(this.mockClaimService.findClaimsToReviewForMediator(
            eq(MEDIATOR_ID), pageableCaptor.capture()))
        .thenReturn(expected);

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(mediator, requested);

    assertSame(expected, result);
    final Pageable usedPageable = pageableCaptor.getValue();
    assertEquals(1, usedPageable.getPageNumber());
    assertEquals(20, usedPageable.getPageSize());
    assertTrue(usedPageable.getSort().isUnsorted());
    verifyNoInteractions(this.mockCampaignService);
  }

  @Test
  void testGetClaimReviewsForAgencyUsesOwnedCampaigns() {
    final BuzzmaUser agency = BuzzmaUser.builder().id(AGENCY_ID).role(UserRole.ROLE_AGENCY).build();
    final Pageable requested = Pageable.ofSize(10);

    final Campaign ownedCampaign = Campaign.builder().id(OWNED_CAMPAIGN_ID).build();
    when(this.mockCampaignService.getByOwnerId(AGENCY_ID))
        .thenReturn(List.of(CampaignSummary.builder().campaign(ownedCampaign).build()));

    final Page<ClaimReviewModel> expected =
        new PageImpl<>(List.of(ClaimReviewModel.builder().build()));
    when(this.mockClaimService.findClaimsToReviewForCampaigns(anyCollection(), any()))
        .thenReturn(expected);

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(agency, requested);

    assertSame(expected, result);
    verify(this.mockClaimService)
        .findClaimsToReviewForCampaigns(campaignIdsCaptor.capture(), any());
    assertEquals(Set.of(OWNED_CAMPAIGN_ID), Set.copyOf(campaignIdsCaptor.getValue()));
  }

  @Test
  void testGetClaimReviewsForAgencyWithNoApplicableCampaignsReturnsEmptyPage() {
    final BuzzmaUser agency = BuzzmaUser.builder().id(AGENCY_ID).role(UserRole.ROLE_AGENCY).build();
    final Pageable requested = Pageable.ofSize(10);

    when(this.mockCampaignService.getByOwnerId(AGENCY_ID)).thenReturn(List.of());

    final Page<ClaimReviewModel> result =
        this.claimReviewService.getClaimReviews(agency, requested);

    assertTrue(result.getContent().isEmpty());
    verify(this.mockClaimService, never()).findClaimsToReviewForCampaigns(anyCollection(), any());
  }
}
