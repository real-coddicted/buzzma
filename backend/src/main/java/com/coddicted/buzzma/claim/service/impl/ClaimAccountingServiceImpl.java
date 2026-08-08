package com.coddicted.buzzma.claim.service.impl;

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
import com.coddicted.buzzma.claim.persistence.projection.AwaitedPaymentProjection;
import com.coddicted.buzzma.claim.persistence.projection.PendingPayoutProjection;
import com.coddicted.buzzma.claim.persistence.projection.ReceivedPaymentProjection;
import com.coddicted.buzzma.claim.service.ClaimAccountingService;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.shared.constants.WellKnownSystemActors;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigInteger;
import java.time.Instant;
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
  private final ClaimService claimService;
  private final DealService dealService;
  private final CampaignService campaignService;
  private final CampaignAssignmentService campaignAssignmentService;
  private final CommissionService commissionService;

  public ClaimAccountingServiceImpl(
      final ClaimAccountingRepository claimAccountingRepository,
      final ClaimService claimService,
      final DealService dealService,
      final CampaignService campaignService,
      final CampaignAssignmentService campaignAssignmentService,
      final CommissionService commissionService) {
    this.claimAccountingRepository = claimAccountingRepository;
    this.claimService = claimService;
    this.dealService = dealService;
    this.campaignService = campaignService;
    this.campaignAssignmentService = campaignAssignmentService;
    this.commissionService = commissionService;
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
    final Commission commission =
        commissionService.getCommissionCharged(claim.getCampaignId(), deal.getOwnerId());

    final BigInteger mediatorReceivablePaise = computeMediatorReceivable(claim, campaignAssignment);
    final BigInteger buyerReceivablePaise = computeBuyerReceivable(claim, commission);

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
    claimService.markAccountingCompleted(claim.getId());

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

  private BigInteger computeBuyerReceivable(final Claim claim, Commission commission) {
    BigInteger amountApproved = claim.getAmountApprovedPaise();
    BigInteger commissionCharged = commission.getCommissionPaise();
    return amountApproved.subtract(commissionCharged);
  }

  // ── Payout / My-payments pass-through queries ──────────────────────────────

  @Override
  @Transactional(readOnly = true)
  public List<PendingPayoutProjection> findPendingByAgency(final UUID agencyId) {
    return claimAccountingRepository.findPendingByAgency(agencyId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PendingPayoutProjection> findPendingByMediator(final UUID mediatorId) {
    return claimAccountingRepository.findPendingByMediator(mediatorId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ClaimAccounting> findClaimsPendingForMediatorPayout(
      final UUID agencyId, final UUID mediatorId) {
    return claimAccountingRepository.findClaimsPendingForMediatorPayout(agencyId, mediatorId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ClaimAccounting> findClaimsPendingForBuyerPayout(
      final UUID mediatorId, final UUID buyerId) {
    return claimAccountingRepository.findClaimsPendingForBuyerPayout(mediatorId, buyerId);
  }

  @Override
  @Transactional
  public List<ClaimAccounting> findByIdInForUpdate(final List<UUID> ids) {
    return claimAccountingRepository.findByIdInForUpdate(ids);
  }

  @Override
  @Transactional
  public List<ClaimAccounting> findPendingByAgencyAndMediatorForUpdate(
      final UUID agencyId, final UUID mediatorId) {
    return claimAccountingRepository.findPendingByAgencyAndMediatorForUpdate(agencyId, mediatorId);
  }

  @Override
  @Transactional
  public List<ClaimAccounting> findPendingByMediatorAndBuyerForUpdate(
      final UUID mediatorId, final UUID buyerId) {
    return claimAccountingRepository.findPendingByMediatorAndBuyerForUpdate(mediatorId, buyerId);
  }

  @Override
  @Transactional
  public void markMediatorPaid(
      final List<UUID> ids,
      final UUID paymentId,
      final Instant paidAt,
      final Instant updatedAt,
      final UUID updatedBy) {
    claimAccountingRepository.markMediatorPaid(ids, paymentId, paidAt, updatedAt, updatedBy);
  }

  @Override
  @Transactional
  public void markBuyerPaid(
      final List<UUID> ids,
      final UUID paymentId,
      final Instant paidAt,
      final Instant updatedAt,
      final UUID updatedBy) {
    claimAccountingRepository.markBuyerPaid(ids, paymentId, paidAt, updatedAt, updatedBy);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ReceivedPaymentProjection> findReceivedByMediator(final UUID mediatorId) {
    return claimAccountingRepository.findReceivedByMediator(mediatorId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ReceivedPaymentProjection> findReceivedByBuyer(final UUID buyerId) {
    return claimAccountingRepository.findReceivedByBuyer(buyerId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<AwaitedPaymentProjection> findAwaitedByMediator(final UUID mediatorId) {
    return claimAccountingRepository.findAwaitedByMediator(mediatorId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<AwaitedPaymentProjection> findAwaitedByBuyer(final UUID buyerId) {
    return claimAccountingRepository.findAwaitedByBuyer(buyerId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ClaimAccounting> findClaimsByMediatorPaymentId(
      final UUID mediatorId, final UUID paymentId) {
    return claimAccountingRepository.findClaimsByMediatorPaymentId(mediatorId, paymentId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ClaimAccounting> findClaimsByBuyerPaymentId(
      final UUID buyerId, final UUID paymentId) {
    return claimAccountingRepository.findClaimsByBuyerPaymentId(buyerId, paymentId);
  }

  @Override
  @Transactional(readOnly = true)
  public long countByMediatorPaymentId(final UUID mediatorPaymentId) {
    return claimAccountingRepository.countByMediatorPaymentId(mediatorPaymentId);
  }

  @Override
  @Transactional(readOnly = true)
  public long countByBuyerPaymentId(final UUID buyerPaymentId) {
    return claimAccountingRepository.countByBuyerPaymentId(buyerPaymentId);
  }
}
