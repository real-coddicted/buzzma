package com.coddicted.buzzma.order.entity;

import com.coddicted.buzzma.shared.common.AuditEntityListener;
import com.coddicted.buzzma.shared.common.Auditable;
import com.coddicted.buzzma.shared.enums.OrderWorkflowStatus;
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
@Table(name = "orders")
@EntityListeners(AuditEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Order implements Auditable {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "campaign_id", nullable = false)
  private UUID campaignId;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 50)
  private OrderWorkflowStatus status;

  @Column(name = "ecommerce_order_id", length = 100)
  private String ecommerceOrderId;

  @Column(name = "amount_paise")
  private BigInteger amountPaise;

  @Column(name = "product_name", length = 255)
  private String productName;

  @Column(name = "seller_name", length = 255)
  private String sellerName;

  // ISO date string YYYY-MM-DD entered by the buyer
  @Column(name = "order_date", length = 10)
  private String orderDate;

  @Column(name = "account_name", length = 255)
  private String accountName;

  @Column(name = "review_url", length = 500)
  private String reviewUrl;

  @Column(name = "order_screenshot_key", length = 500)
  private String orderScreenshotKey;

  @Column(name = "review_screenshot_key", length = 500)
  private String reviewScreenshotKey;

  @Column(name = "return_screenshot_key", length = 500)
  private String returnScreenshotKey;

  @Column(name = "rejection_note", columnDefinition = "TEXT")
  private String rejectionNote;

  @Column(name = "created_by")
  private UUID createdBy;

  @Column(name = "updated_by")
  private UUID updatedBy;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted = false;
}
