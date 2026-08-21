package com.coddicted.buzzma.claim.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.campaign.service.CampaignTypeStepService;
import com.coddicted.buzzma.campaign.service.DealService;
import com.coddicted.buzzma.claim.dto.ClaimResponseDto;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.mapper.ClaimMapper;
import com.coddicted.buzzma.claim.mapper.ClaimReviewMapper;
import com.coddicted.buzzma.claim.processor.ClaimReviewProcessor;
import com.coddicted.buzzma.claim.service.ClaimReviewService;
import com.coddicted.buzzma.claim.service.ClaimService;
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
  private static final UUID DEAL_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID CLAIM_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

  private ClaimService claimService;
  private DealService dealService;
  private CampaignTypeStepService campaignTypeStepService;
  private ClaimMapper claimMapper;
  private ClaimController controller;

  @BeforeEach
  void setUp() {
    this.claimService = Mockito.mock(ClaimService.class);
    final ClaimReviewService claimReviewService = Mockito.mock(ClaimReviewService.class);
    this.dealService = Mockito.mock(DealService.class);
    this.campaignTypeStepService = Mockito.mock(CampaignTypeStepService.class);
    this.claimMapper = Mockito.mock(ClaimMapper.class);
    final ClaimReviewMapper claimReviewMapper = Mockito.mock(ClaimReviewMapper.class);
    final ClaimReviewProcessor claimReviewProcessor = Mockito.mock(ClaimReviewProcessor.class);
    this.controller =
        new ClaimController(
            this.claimService,
            claimReviewService,
            this.dealService,
            this.campaignTypeStepService,
            this.claimMapper,
            claimReviewMapper,
            claimReviewProcessor);
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

    final ClaimResponseDto mappedDto = ClaimResponseDto.builder().id(CLAIM_ID).build();
    when(this.claimMapper.toResponse(claim, deal, List.of(), 0)).thenReturn(mappedDto);

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
}
