package com.coddicted.buzzma.claim.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.campaign.dto.DealResponseDto;
import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.campaign.service.CampaignTypeStepService;
import com.coddicted.buzzma.campaign.service.DealService;
import com.coddicted.buzzma.claim.dto.ClaimResponseDto;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimStatus;
import com.coddicted.buzzma.claim.mapper.ClaimMapper;
import com.coddicted.buzzma.claim.mapper.ClaimReviewMapper;
import com.coddicted.buzzma.claim.processor.ClaimReviewProcessor;
import com.coddicted.buzzma.claim.service.ClaimReviewService;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.service.UserService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class ClaimControllerTest {

  private static final UUID REQUESTER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID MEDIATOR_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID CLAIM_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
  private static final UUID DEAL_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
  private static final String MEDIATOR_NAME = "Alice Mediator";

  private ClaimService claimService;
  private ClaimReviewService claimReviewService;
  private DealService dealService;
  private CampaignTypeStepService campaignTypeStepService;
  private ClaimMapper claimMapper;
  private ClaimReviewMapper claimReviewMapper;
  private ClaimReviewProcessor claimReviewProcessor;
  private UserService userService;
  private ClaimController controller;

  @BeforeEach
  void setUp() {
    this.claimService = Mockito.mock(ClaimService.class);
    this.claimReviewService = Mockito.mock(ClaimReviewService.class);
    this.dealService = Mockito.mock(DealService.class);
    this.campaignTypeStepService = Mockito.mock(CampaignTypeStepService.class);
    this.claimMapper = Mockito.mock(ClaimMapper.class);
    this.claimReviewMapper = Mockito.mock(ClaimReviewMapper.class);
    this.claimReviewProcessor = Mockito.mock(ClaimReviewProcessor.class);
    this.userService = Mockito.mock(UserService.class);
    this.controller =
        new ClaimController(
            this.claimService,
            this.claimReviewService,
            this.dealService,
            this.campaignTypeStepService,
            this.claimMapper,
            this.claimReviewMapper,
            this.claimReviewProcessor,
            this.userService);
  }

  @Test
  void testListMapsPageOfClaimsToPagedResponse() {
    final Claim claim = Claim.builder().id(CLAIM_ID).dealId(DEAL_ID).build();
    when(this.claimService.listByOwner(REQUESTER_ID, 0, 10))
        .thenReturn(new PageImpl<>(List.of(claim), PageRequest.of(0, 10), 1));

    final Campaign campaign = Campaign.builder().type(CampaignType.CAMPAIGN_TYPE_ORDER).build();
    final Deal deal = Deal.builder().id(DEAL_ID).campaign(campaign).build();
    when(this.dealService.getById(DEAL_ID)).thenReturn(deal);

    when(this.claimService.listScreenshots(CLAIM_ID)).thenReturn(List.of());
    when(this.campaignTypeStepService.getStepConfig()).thenReturn(Map.of());

    final DealResponseDto mappedDeal = DealResponseDto.builder().id(DEAL_ID).build();
    final ClaimResponseDto mappedDto =
        ClaimResponseDto.builder().id(CLAIM_ID).deal(mappedDeal).build();
    when(this.claimMapper.toResponse(claim, deal, List.of(), 0)).thenReturn(mappedDto);
    when(this.userService.getByIds(List.of())).thenReturn(List.of());

    final var result = this.controller.list(REQUESTER_ID, 0, 10);

    assertThat(result.getItems()).containsExactly(mappedDto);
    assertThat(result.getTotal()).isEqualTo(1);
    assertThat(result.getPage()).isZero();
    assertThat(result.getTotalPages()).isEqualTo(1);
  }

  @Test
  void testListWithNoClaimsReturnsEmptyPagedResponse() {
    when(this.claimService.listByOwner(REQUESTER_ID, 0, 10))
        .thenReturn(Page.empty(PageRequest.of(0, 10)));

    final var result = this.controller.list(REQUESTER_ID, 0, 10);

    assertThat(result.getItems()).isEmpty();
    assertThat(result.getTotal()).isZero();
  }

  @Test
  void testListEnrichesItemsDealWithOwnerName() {
    final Campaign campaign = Campaign.builder().type(CampaignType.CAMPAIGN_TYPE_ORDER).build();
    final Claim claim =
        Claim.builder().id(CLAIM_ID).dealId(DEAL_ID).status(ClaimStatus.ORDERED).build();
    final Deal deal = Deal.builder().id(DEAL_ID).ownerId(MEDIATOR_ID).campaign(campaign).build();

    when(this.claimService.listByOwner(REQUESTER_ID, 0, 10))
        .thenReturn(new PageImpl<>(List.of(claim), PageRequest.of(0, 10), 1));
    when(this.dealService.getById(DEAL_ID)).thenReturn(deal);
    when(this.claimService.listScreenshots(CLAIM_ID)).thenReturn(List.of());
    when(this.campaignTypeStepService.getStepConfig()).thenReturn(Map.of());

    final DealResponseDto mappedDeal =
        DealResponseDto.builder().id(DEAL_ID).ownerId(MEDIATOR_ID).build();
    final ClaimResponseDto mappedClaim =
        ClaimResponseDto.builder().id(CLAIM_ID).deal(mappedDeal).build();
    when(this.claimMapper.toResponse(claim, deal, List.of(), 0)).thenReturn(mappedClaim);

    final BuzzmaUser mediator =
        BuzzmaUser.builder()
            .id(MEDIATOR_ID)
            .name(MEDIATOR_NAME)
            .role(UserRole.ROLE_MEDIATOR)
            .build();
    when(this.userService.getByIds(List.of(MEDIATOR_ID))).thenReturn(List.of(mediator));

    final var result = this.controller.list(REQUESTER_ID, 0, 10);

    assertThat(result.getItems()).hasSize(1);
    assertThat(result.getItems().get(0).getDeal().getOwnerName()).isEqualTo(MEDIATOR_NAME);
  }
}
