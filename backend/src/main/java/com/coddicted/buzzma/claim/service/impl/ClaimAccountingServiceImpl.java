package com.coddicted.buzzma.claim.service.impl;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignAssignment;
import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.campaign.service.CampaignAssignmentService;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.campaign.service.DealService;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimAccounting;
import com.coddicted.buzzma.claim.persistence.ClaimAccountingRepository;
import com.coddicted.buzzma.claim.persistence.ClaimRepository;
import com.coddicted.buzzma.claim.service.ClaimAccountingService;
import com.coddicted.buzzma.shared.constants.WellKnownSystemActors;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClaimAccountingServiceImpl implements ClaimAccountingService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClaimAccountingServiceImpl.class);

  @PersistenceContext private EntityManager entityManager;

  private final ClaimAccountingRepository claimAccountingRepository;
  private final ClaimRepository claimRepository;
  private final DealService dealService;
  private final CampaignService campaignService;
  private final CampaignAssignmentService campaignAssignmentService;

  public ClaimAccountingServiceImpl(
      final ClaimAccountingRepository claimAccountingRepository,
      final ClaimRepository claimRepository,
      final DealService dealService,
      final CampaignService campaignService,
      CampaignAssignmentService campaignAssignmentService) {
    this.claimAccountingRepository = claimAccountingRepository;
    this.claimRepository = claimRepository;
    this.dealService = dealService;
    this.campaignService = campaignService;
    this.campaignAssignmentService = campaignAssignmentService;
  }

  @Override
  @Transactional
  @SuppressWarnings("unchecked")
  public List<UUID> claimBatchForProcessing(final int batchSize, final int maxRetries) {
    List<Object> rawIds =
        entityManager
            .createNativeQuery(
                """
            UPDATE claims
            SET accounting_status            = 'IN_PROGRESS',
                accounting_last_attempted_at = NOW()
            WHERE id IN (
                SELECT id FROM claims
                WHERE accounting_status IN ('PENDING', 'FAILED')
                  AND accounting_retry_count < :maxRetries
                  AND status = 'APPROVED'
                  AND is_deleted = false
                ORDER BY updated_at
                LIMIT :batchSize
                FOR UPDATE SKIP LOCKED
            )
            RETURNING id
            """)
            .setParameter("maxRetries", maxRetries)
            .setParameter("batchSize", batchSize)
            .getResultList();

    return rawIds.stream()
        .map(id -> id instanceof UUID ? (UUID) id : UUID.fromString(id.toString()))
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void processAccounting(final Claim claim) {
    final Deal deal = dealService.getById(claim.getDealId());
    final Campaign campaign = campaignService.getById(claim.getCampaignId());
    final CampaignAssignment campaignAssignment =
        campaignAssignmentService.getByCampaignIdAndAssignorIdAndAssigneeId(
            campaign.getId(), campaign.getOwnerId(), deal.getOwnerId());

    final BigInteger mediatorReceivablePaise = computeMediatorReceivable(claim, campaignAssignment);
    final BigInteger buyerReceivablePaise = computeBuyerReceivable(claim, deal);

    final ClaimAccounting accounting =
        ClaimAccounting.builder()
            .claimId(claim.getId())
            .campaignId(claim.getCampaignId())
            .dealId(claim.getDealId())
            .buyerId(claim.getOwnerId())
            .mediatorId(deal.getOwnerId())
            .agencyId(campaign.getOwnerId())
            .mediatorReceivablePaise(mediatorReceivablePaise)
            .buyerReceivablePaise(buyerReceivablePaise)
            .createdBy(WellKnownSystemActors.SYSTEM_USER_ID)
            .updatedBy(WellKnownSystemActors.SYSTEM_USER_ID)
            .build();

    claimAccountingRepository.save(accounting);
    claimRepository.markAccountingCompleted(claim.getId());

    LOGGER.info(
        "ClaimAccounting processed for claim {} — mediator: {} paise, buyer: {} paise",
        claim.getId(),
        mediatorReceivablePaise,
        buyerReceivablePaise);
  }

  private BigInteger computeMediatorReceivable(
      final Claim claim, CampaignAssignment campaignAssignment) {
    BigInteger amountApproved = claim.getAmountApprovedPaise();
    BigInteger commissionOffered = campaignAssignment.getCommissionOfferedPaise();
    return amountApproved.add(commissionOffered);
  }

  private BigInteger computeBuyerReceivable(final Claim claim, Deal deal) {
    BigInteger amountApproved = claim.getAmountApprovedPaise();
    BigInteger dealPrice = deal.getDealPricePaise();
    return amountApproved.subtract(dealPrice);
  }
}
