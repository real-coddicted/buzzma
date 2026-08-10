package com.coddicted.buzzma.claim.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "claim_review_worksheet_rows")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ClaimReviewWorksheetRow {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "worksheet_id", nullable = false)
  private UUID worksheetId;

  @Column(name = "campaign", length = 500)
  private String campaign;

  @Column(name = "campaign_code", length = 500)
  private String campaignCode;

  @Column(name = "brand", length = 500)
  private String brand;

  @Column(name = "mediator", length = 500)
  private String mediator;

  @Column(name = "buyer", length = 500)
  private String buyer;

  @Column(name = "profile_name", length = 500)
  private String profileName;

  @Column(name = "platform", length = 500)
  private String platform;

  @Column(name = "order_id", length = 500)
  private String orderId;

  @Column(name = "order_date", length = 500)
  private String orderDate;

  @Column(name = "order_amount", length = 500)
  private String orderAmount;

  @Column(name = "claim_code", length = 500)
  private String claimCode;

  @Column(name = "claim_status", length = 500)
  private String claimStatus;

  @Column(name = "match_score", length = 500)
  private String matchScore;

  @Column(name = "review_status", length = 500)
  private String reviewStatus;

  @Column(name = "amount_approved", length = 500)
  private String amountApproved;

  @Column(name = "brand_review", length = 500)
  private String brandReview;

  @Column(name = "remarks", columnDefinition = "TEXT")
  private String remarks;

  @Enumerated(EnumType.STRING)
  @Column(name = "processing_status", nullable = false, length = 20)
  private WorksheetRowStatus processingStatus;

  @Column(name = "error_remarks", columnDefinition = "TEXT")
  private String errorRemarks;

  @Column(name = "retry_count", nullable = false)
  @Builder.Default
  private int retryCount = 0;

  @Column(name = "last_attempted_at")
  private Instant lastAttemptedAt;
}
