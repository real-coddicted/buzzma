package com.coddicted.buzzma.claim.persistence;

import com.coddicted.buzzma.claim.entity.ClaimAccounting;
import com.coddicted.buzzma.claim.persistence.projection.AwaitedPaymentProjection;
import com.coddicted.buzzma.claim.persistence.projection.PendingPayoutProjection;
import com.coddicted.buzzma.claim.persistence.projection.ReceivedPaymentProjection;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaimAccountingRepository extends JpaRepository<ClaimAccounting, UUID> {

  boolean existsByClaimId(UUID claimId);

  // ── Payout pending lists ──────────────────────────────────────────────────

  @Query(
      """
      SELECT ca.mediatorId AS payeeId,
             COUNT(ca) AS claimCount,
             SUM(ca.mediatorReceivablePaise) AS totalAmountPaise,
             MIN(ca.createdAt) AS oldestClaimAt
      FROM ClaimAccounting ca
      WHERE ca.agencyId = :agencyId
        AND ca.mediatorPaymentStatus
            = com.coddicted.buzzma.claim.entity.AccountingPaymentStatus.PENDING
      GROUP BY ca.mediatorId
      ORDER BY MIN(ca.createdAt) ASC
      """)
  List<PendingPayoutProjection> findPendingByAgency(@Param("agencyId") UUID agencyId);

  @Query(
      """
      SELECT ca.buyerId AS payeeId,
             COUNT(ca) AS claimCount,
             SUM(ca.buyerReceivablePaise) AS totalAmountPaise,
             MIN(ca.createdAt) AS oldestClaimAt
      FROM ClaimAccounting ca
      WHERE ca.mediatorId = :mediatorId
        AND ca.buyerPaymentStatus
            = com.coddicted.buzzma.claim.entity.AccountingPaymentStatus.PENDING
      GROUP BY ca.buyerId
      ORDER BY MIN(ca.createdAt) ASC
      """)
  List<PendingPayoutProjection> findPendingByMediator(@Param("mediatorId") UUID mediatorId);

  // ── Payout drill-down (no lock — read-only for display) ──────────────────

  @Query(
      """
      SELECT ca FROM ClaimAccounting ca
      WHERE ca.agencyId = :agencyId
        AND ca.mediatorId = :mediatorId
        AND ca.mediatorPaymentStatus
            = com.coddicted.buzzma.claim.entity.AccountingPaymentStatus.PENDING
      ORDER BY ca.createdAt ASC
      """)
  List<ClaimAccounting> findClaimsForMediatorPayout(
      @Param("agencyId") UUID agencyId, @Param("mediatorId") UUID mediatorId);

  @Query(
      """
      SELECT ca FROM ClaimAccounting ca
      WHERE ca.mediatorId = :mediatorId
        AND ca.buyerId = :buyerId
        AND ca.buyerPaymentStatus
            = com.coddicted.buzzma.claim.entity.AccountingPaymentStatus.PENDING
      ORDER BY ca.createdAt ASC
      """)
  List<ClaimAccounting> findClaimsForBuyerPayout(
      @Param("mediatorId") UUID mediatorId, @Param("buyerId") UUID buyerId);

  // ── Locking queries for pay action ───────────────────────────────────────

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT ca FROM ClaimAccounting ca WHERE ca.id IN :ids")
  List<ClaimAccounting> findByIdInForUpdate(@Param("ids") List<UUID> ids);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      """
      SELECT ca FROM ClaimAccounting ca
      WHERE ca.agencyId = :agencyId
        AND ca.mediatorId = :mediatorId
        AND ca.mediatorPaymentStatus
            = com.coddicted.buzzma.claim.entity.AccountingPaymentStatus.PENDING
      """)
  List<ClaimAccounting> findPendingByAgencyAndMediatorForUpdate(
      @Param("agencyId") UUID agencyId, @Param("mediatorId") UUID mediatorId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      """
      SELECT ca FROM ClaimAccounting ca
      WHERE ca.mediatorId = :mediatorId
        AND ca.buyerId = :buyerId
        AND ca.buyerPaymentStatus
            = com.coddicted.buzzma.claim.entity.AccountingPaymentStatus.PENDING
      """)
  List<ClaimAccounting> findPendingByMediatorAndBuyerForUpdate(
      @Param("mediatorId") UUID mediatorId, @Param("buyerId") UUID buyerId);

  // ── Bulk status updates ───────────────────────────────────────────────────

  @Modifying(clearAutomatically = true)
  @Query(
      """
      UPDATE ClaimAccounting ca
      SET ca.mediatorPaymentStatus
              = com.coddicted.buzzma.claim.entity.AccountingPaymentStatus.PAID,
          ca.mediatorPaidAt = :paidAt,
          ca.mediatorPaymentId = :paymentId,
          ca.updatedAt = :updatedAt,
          ca.updatedBy = :updatedBy
      WHERE ca.id IN :ids
      """)
  void markMediatorPaid(
      @Param("ids") List<UUID> ids,
      @Param("paymentId") UUID paymentId,
      @Param("paidAt") Instant paidAt,
      @Param("updatedAt") Instant updatedAt,
      @Param("updatedBy") UUID updatedBy);

  @Modifying(clearAutomatically = true)
  @Query(
      """
      UPDATE ClaimAccounting ca
      SET ca.buyerPaymentStatus
              = com.coddicted.buzzma.claim.entity.AccountingPaymentStatus.PAID,
          ca.buyerPaidAt = :paidAt,
          ca.buyerPaymentId = :paymentId,
          ca.updatedAt = :updatedAt,
          ca.updatedBy = :updatedBy
      WHERE ca.id IN :ids
      """)
  void markBuyerPaid(
      @Param("ids") List<UUID> ids,
      @Param("paymentId") UUID paymentId,
      @Param("paidAt") Instant paidAt,
      @Param("updatedAt") Instant updatedAt,
      @Param("updatedBy") UUID updatedBy);

  // ── My Payments — received ────────────────────────────────────────────────

  @Query(
      """
      SELECT ca.mediatorPaymentId AS paymentId,
             ca.agencyId AS payerId,
             COUNT(ca) AS claimCount,
             SUM(ca.mediatorReceivablePaise) AS totalAmountPaise,
             MAX(ca.mediatorPaidAt) AS paidAt
      FROM ClaimAccounting ca
      WHERE ca.mediatorId = :mediatorId
        AND ca.mediatorPaymentStatus
            = com.coddicted.buzzma.claim.entity.AccountingPaymentStatus.PAID
      GROUP BY ca.mediatorPaymentId, ca.agencyId
      ORDER BY MAX(ca.mediatorPaidAt) DESC
      """)
  List<ReceivedPaymentProjection> findReceivedByMediator(@Param("mediatorId") UUID mediatorId);

  @Query(
      """
      SELECT ca.buyerPaymentId AS paymentId,
             ca.mediatorId AS payerId,
             COUNT(ca) AS claimCount,
             SUM(ca.buyerReceivablePaise) AS totalAmountPaise,
             MAX(ca.buyerPaidAt) AS paidAt
      FROM ClaimAccounting ca
      WHERE ca.buyerId = :buyerId
        AND ca.buyerPaymentStatus
            = com.coddicted.buzzma.claim.entity.AccountingPaymentStatus.PAID
      GROUP BY ca.buyerPaymentId, ca.mediatorId
      ORDER BY MAX(ca.buyerPaidAt) DESC
      """)
  List<ReceivedPaymentProjection> findReceivedByBuyer(@Param("buyerId") UUID buyerId);

  // ── My Payments — awaited ─────────────────────────────────────────────────

  @Query(
      """
      SELECT ca.agencyId AS counterpartyId,
             COUNT(ca) AS claimCount,
             SUM(ca.mediatorReceivablePaise) AS totalAmountPaise,
             MIN(ca.createdAt) AS oldestClaimAt
      FROM ClaimAccounting ca
      WHERE ca.mediatorId = :mediatorId
        AND ca.mediatorPaymentStatus
            = com.coddicted.buzzma.claim.entity.AccountingPaymentStatus.PENDING
      GROUP BY ca.agencyId
      ORDER BY MIN(ca.createdAt) ASC
      """)
  List<AwaitedPaymentProjection> findAwaitedByMediator(@Param("mediatorId") UUID mediatorId);

  @Query(
      """
      SELECT ca.mediatorId AS counterpartyId,
             COUNT(ca) AS claimCount,
             SUM(ca.buyerReceivablePaise) AS totalAmountPaise,
             MIN(ca.createdAt) AS oldestClaimAt
      FROM ClaimAccounting ca
      WHERE ca.buyerId = :buyerId
        AND ca.buyerPaymentStatus
            = com.coddicted.buzzma.claim.entity.AccountingPaymentStatus.PENDING
      GROUP BY ca.mediatorId
      ORDER BY MIN(ca.createdAt) ASC
      """)
  List<AwaitedPaymentProjection> findAwaitedByBuyer(@Param("buyerId") UUID buyerId);

  // ── Claim count per payment (for receipt) ────────────────────────────────

  long countByMediatorPaymentId(UUID mediatorPaymentId);

  long countByBuyerPaymentId(UUID buyerPaymentId);
}
