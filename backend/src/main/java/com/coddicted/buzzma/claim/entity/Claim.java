package com.coddicted.buzzma.claim.entity;

import com.coddicted.buzzma.shared.common.AuditEntityListener;
import com.coddicted.buzzma.shared.common.Auditable;
import com.coddicted.buzzma.shared.enums.Platform;
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
@Table(name = "claims")
@EntityListeners(AuditEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Claim implements Auditable {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "campaign_id", nullable = false)
  private UUID campaignId;

  @Column(name = "deal_id", nullable = false)
  private UUID dealId;

  @Column(name = "owner_id", nullable = false)
  private UUID ownerId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 50)
  private ClaimStatus status;

  @Enumerated(EnumType.STRING)
  @Column(name = "review_status", nullable = false, length = 50)
  private ClaimReviewStatus reviewStatus;

  @Column(name = "ecommerce_order_id", length = 100)
  private String ecommerceOrderId;

  @Enumerated(EnumType.STRING)
  @Column(name = "platform", nullable = false)
  private Platform platform;

  @Column(name = "amount_paise")
  private BigInteger amountPaise;

  @Column(name = "product_name", length = 255)
  private String productName;

  @Column(name = "seller_name", length = 255)
  private String sellerName;

  @Column(name = "order_date")
  private int orderDate;

  @Column(name = "account_name", length = 255)
  private String accountName;

  @Column(name = "review_url", length = 500)
  private String reviewUrl;

  @Column(name = "mediator_verified")
  private Boolean mediatorVerified;

  @Column(name = "score")
  private BigInteger score;

  @Column(name = "reviewer_comments", columnDefinition = "TEXT")
  private String reviewerComments;

  @Column(name = "reviewer_id")
  private UUID reviewerId;

  @Column(name = "created_by")
  private UUID createdBy;

  @Column(name = "updated_by")
  private UUID updatedBy;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "is_deleted", nullable = false)
  @Builder.Default
  private Boolean isDeleted = false;
}
