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
@Table(name = "payments")
@EntityListeners(AuditEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Payment implements Auditable {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  // Generic payer: agency for hop-1 (agency→mediator), mediator for hop-2 (mediator→buyer)
  @Column(name = "payer_id", nullable = false)
  private UUID payerId;

  // Generic payee: mediator for hop-1, buyer for hop-2
  @Column(name = "payee_id", nullable = false)
  private UUID payeeId;

  @Column(name = "screenshot_storage_key", length = 500)
  private String screenshotStorageKey;

  @Column(name = "amount_paid_paise", nullable = false)
  private BigInteger amountPaidPaise;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_method", nullable = false, length = 20)
  private PaymentMethod paymentMethod;

  @Column(name = "utr_ref", length = 100)
  private String utrRef;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;

  @Column(name = "paid_at", nullable = false)
  private Instant paidAt;

  @Column(name = "created_by")
  private UUID createdBy;

  @Column(name = "updated_by")
  private UUID updatedBy;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
