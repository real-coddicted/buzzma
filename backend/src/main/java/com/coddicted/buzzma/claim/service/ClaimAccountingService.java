package com.coddicted.buzzma.claim.service;

import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimAccounting;
import com.coddicted.buzzma.claim.persistence.projection.AwaitedPaymentProjection;
import com.coddicted.buzzma.claim.persistence.projection.MadePaymentProjection;
import com.coddicted.buzzma.claim.persistence.projection.PaidPayoutProjection;
import com.coddicted.buzzma.claim.persistence.projection.PendingPayoutProjection;
import com.coddicted.buzzma.claim.persistence.projection.ReceivedPaymentProjection;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClaimAccountingService {

  List<UUID> claimBatchForProcessing(int batchSize, int maxRetries);

  void processAccounting(Claim claim);

  Optional<ClaimAccounting> getByClaimId(UUID claimId);

  List<ClaimAccounting> getByClaimIdIn(Collection<UUID> claimIds);

  // ── Payout pending lists ──────────────────────────────────────────────────

  List<PendingPayoutProjection> findPendingByAgency(UUID agencyId);

  List<PendingPayoutProjection> findPendingByMediator(UUID mediatorId);

  // ── Payout drill-down (no lock — read-only for display) ──────────────────

  List<ClaimAccounting> findClaimsPendingForMediatorPayout(UUID agencyId, UUID mediatorId);

  List<ClaimAccounting> findClaimsPendingForBuyerPayout(UUID mediatorId, UUID buyerId);

  // ── Locking queries for pay action ───────────────────────────────────────

  List<ClaimAccounting> findByIdInForUpdate(List<UUID> ids);

  List<ClaimAccounting> findPendingByAgencyAndMediatorForUpdate(UUID agencyId, UUID mediatorId);

  List<ClaimAccounting> findPendingByMediatorAndBuyerForUpdate(UUID mediatorId, UUID buyerId);

  // ── Bulk status updates ───────────────────────────────────────────────────

  void markMediatorPaid(
      List<UUID> ids, UUID paymentId, Instant paidAt, Instant updatedAt, UUID updatedBy);

  void markBuyerPaid(
      List<UUID> ids, UUID paymentId, Instant paidAt, Instant updatedAt, UUID updatedBy);

  // ── My Payments — received / awaited ──────────────────────────────────────

  List<ReceivedPaymentProjection> findReceivedByMediator(UUID mediatorId);

  List<ReceivedPaymentProjection> findReceivedByBuyer(UUID buyerId);

  List<AwaitedPaymentProjection> findAwaitedByMediator(UUID mediatorId);

  List<AwaitedPaymentProjection> findAwaitedByBuyer(UUID buyerId);

  // ── My Payments — received claims drill-down ──────────────────────────────

  List<ClaimAccounting> findClaimsByMediatorPaymentId(UUID mediatorId, UUID paymentId);

  List<ClaimAccounting> findClaimsByBuyerPaymentId(UUID buyerId, UUID paymentId);

  // ── Claim count per payment (for receipt) ────────────────────────────────

  long countByMediatorPaymentId(UUID mediatorPaymentId);

  long countByBuyerPaymentId(UUID buyerPaymentId);

  // ── Payouts — paid / made (payer-side history, paginated) ──────────────────

  Page<PaidPayoutProjection> findPaidByAgency(UUID agencyId, Pageable pageable);

  Page<PaidPayoutProjection> findPaidByMediator(UUID mediatorId, Pageable pageable);

  Page<MadePaymentProjection> findPaymentsPaidToMediator(
      UUID agencyId, UUID mediatorId, Pageable pageable);

  Page<MadePaymentProjection> findPaymentsPaidToBuyer(
      UUID mediatorId, UUID buyerId, Pageable pageable);

  Page<ClaimAccounting> findClaimsPaidToMediatorByPayment(
      UUID agencyId, UUID paymentId, Pageable pageable);

  Page<ClaimAccounting> findClaimsPaidToBuyerByPayment(
      UUID mediatorId, UUID paymentId, Pageable pageable);
}
