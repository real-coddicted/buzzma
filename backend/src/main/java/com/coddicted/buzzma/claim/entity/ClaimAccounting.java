package com.coddicted.buzzma.claim.entity;

import com.coddicted.buzzma.shared.common.AuditEntityListener;
import com.coddicted.buzzma.shared.common.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "claim_accountings")
@EntityListeners(AuditEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ClaimAccounting implements Auditable {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  // FK to claims — unique; one accounting record per claim
  @Column(name = "claim_id", nullable = false, unique = true)
  private UUID claimId;

  // Denormalized from claim for efficient filtering without joins
  @Column(name = "campaign_id", nullable = false)
  private UUID campaignId;

  @Column(name = "deal_id", nullable = false)
  private UUID dealId;

  // buyer_id = claim.owner_id
  @Column(name = "buyer_id", nullable = false)
  private UUID buyerId;

  // mediator_id = deal.owner_id (resolved via claim.deal_id → deals.owner_id)
  @Column(name = "mediator_id", nullable = false)
  private UUID mediatorId;

  // agency_id = campaign.owner_id
  @Column(name = "agency_id", nullable = false)
  private UUID agencyId;

  // Hop 1: agency → mediator
  @Column(name = "mediator_receivable_paise", nullable = false)
  private BigInteger mediatorReceivablePaise;

  // Hop 2: mediator → buyer
  @Column(name = "buyer_receivable_paise", nullable = false)
  private BigInteger buyerReceivablePaise;

  // Campaign cashback (ref #739), already included in both receivable amounts above; kept
  // separately for traceability. Null when the campaign offers no cashback.
  @Column(name = "additional_reward_cashback_paise")
  private BigInteger additionalRewardCashbackPaise;

  @Enumerated(EnumType.STRING)
  @Column(name = "mediator_payment_status", nullable = false, length = 20)
  @Builder.Default
  private AccountingPaymentStatus mediatorPaymentStatus = AccountingPaymentStatus.PENDING;

  @Column(name = "mediator_paid_at")
  private Instant mediatorPaidAt;

  @Column(name = "mediator_payment_id")
  private UUID mediatorPaymentId;

  @Enumerated(EnumType.STRING)
  @Column(name = "buyer_payment_status", nullable = false, length = 20)
  @Builder.Default
  private AccountingPaymentStatus buyerPaymentStatus = AccountingPaymentStatus.PENDING;

  @Column(name = "buyer_paid_at")
  private Instant buyerPaidAt;

  @Column(name = "buyer_payment_id")
  private UUID buyerPaymentId;

  @Column(name = "created_by")
  private UUID createdBy;

  @Column(name = "updated_by")
  private UUID updatedBy;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
