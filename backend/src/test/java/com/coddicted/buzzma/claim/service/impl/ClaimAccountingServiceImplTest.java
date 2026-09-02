package com.coddicted.buzzma.claim.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignAssignment;
import com.coddicted.buzzma.campaign.entity.Commission;
import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.campaign.service.CampaignAssignmentService;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.campaign.service.CommissionService;
import com.coddicted.buzzma.campaign.service.DealService;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimAccounting;
import com.coddicted.buzzma.claim.persistence.ClaimAccountingRepository;
import com.coddicted.buzzma.claim.service.ClaimService;
import java.math.BigInteger;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClaimAccountingServiceImplTest {

  private static final UUID CLAIM_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID CAMPAIGN_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID DEAL_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
  private static final UUID BUYER_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
  private static final UUID MEDIATOR_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
  private static final UUID AGENCY_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");

  @Mock private ClaimAccountingRepository claimAccountingRepository;
  @Mock private ClaimService claimService;
  @Mock private DealService dealService;
  @Mock private CampaignService campaignService;
  @Mock private CampaignAssignmentService campaignAssignmentService;
  @Mock private CommissionService commissionService;

  private ClaimAccountingServiceImpl service;

  @BeforeEach
  void setUp() {
    service =
        new ClaimAccountingServiceImpl(
            claimAccountingRepository,
            claimService,
            dealService,
            campaignService,
            campaignAssignmentService,
            commissionService);
  }

  private ClaimAccounting process(
      final BigInteger amountApprovedPaise,
      final BigInteger cashbackPaise,
      final BigInteger commissionOfferedPaise,
      final BigInteger commissionChargedPaise) {
    final Claim claim =
        Claim.builder()
            .id(CLAIM_ID)
            .campaignId(CAMPAIGN_ID)
            .dealId(DEAL_ID)
            .ownerId(BUYER_ID)
            .amountApprovedPaise(amountApprovedPaise)
            .build();
    when(dealService.getById(DEAL_ID))
        .thenReturn(Deal.builder().id(DEAL_ID).ownerId(MEDIATOR_ID).build());
    when(campaignService.getById(CAMPAIGN_ID))
        .thenReturn(
            Campaign.builder()
                .id(CAMPAIGN_ID)
                .ownerId(AGENCY_ID)
                .additionalRewardCashbackPaise(cashbackPaise)
                .build());
    when(campaignAssignmentService.getByCampaignIdAndAssignorIdAndAssigneeId(
            CAMPAIGN_ID, AGENCY_ID, MEDIATOR_ID))
        .thenReturn(
            CampaignAssignment.builder().commissionOfferedPaise(commissionOfferedPaise).build());
    when(commissionService.getCommissionCharged(CAMPAIGN_ID, MEDIATOR_ID))
        .thenReturn(Commission.builder().commissionPaise(commissionChargedPaise).build());

    service.processAccounting(claim);

    final ArgumentCaptor<ClaimAccounting> captor = ArgumentCaptor.forClass(ClaimAccounting.class);
    verify(claimAccountingRepository).save(captor.capture());
    verify(claimService).markAccountingCompleted(CLAIM_ID);
    return captor.getValue();
  }

  @Test
  void testCashbackIsAddedToBothReceivables() {
    final ClaimAccounting accounting =
        process(
            BigInteger.valueOf(100_000),
            BigInteger.valueOf(50_000),
            BigInteger.valueOf(20_000),
            BigInteger.valueOf(10_000));

    assertEquals(BigInteger.valueOf(170_000), accounting.getMediatorReceivablePaise());
    assertEquals(BigInteger.valueOf(140_000), accounting.getBuyerReceivablePaise());
    assertEquals(BigInteger.valueOf(50_000), accounting.getAdditionalRewardCashbackPaise());
  }

  @Test
  void testNoCashbackLeavesReceivablesUnchanged() {
    final ClaimAccounting accounting =
        process(
            BigInteger.valueOf(100_000),
            null,
            BigInteger.valueOf(20_000),
            BigInteger.valueOf(10_000));

    assertEquals(BigInteger.valueOf(120_000), accounting.getMediatorReceivablePaise());
    assertEquals(BigInteger.valueOf(90_000), accounting.getBuyerReceivablePaise());
    assertNull(accounting.getAdditionalRewardCashbackPaise());
  }

  @Test
  void testFreeAppReviewWithCashbackPaysCommissionPlusCashback() {
    final ClaimAccounting accounting =
        process(
            BigInteger.ZERO,
            BigInteger.valueOf(50_000),
            BigInteger.valueOf(20_000),
            BigInteger.ZERO);

    assertEquals(BigInteger.valueOf(70_000), accounting.getMediatorReceivablePaise());
    assertEquals(BigInteger.valueOf(50_000), accounting.getBuyerReceivablePaise());
  }
}
