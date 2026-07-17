package com.coddicted.buzzma.claim.entity;

import com.coddicted.buzzma.extraction.entity.ScoredValue;
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
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "claim_screenshots")
@EntityListeners(AuditEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ClaimScreenshot implements Auditable {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "claim_id", nullable = false, updatable = false)
  private UUID claimId;

  @Column(name = "storage_key", nullable = false, length = 500)
  private String storageKey;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private ScreenshotType type;

  @Enumerated(EnumType.STRING)
  @Column(name = "verification_status", nullable = false)
  private ScreenshotVerificationStatus verificationStatus;

  @Column(name = "score")
  private Integer score;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "extracted_details", columnDefinition = "jsonb")
  private Map<String, ScoredValue> extractedDetails;

  @Column(name = "reviewer_comments", columnDefinition = "TEXT")
  private String reviewerComments;

  @Column(name = "created_by")
  private UUID createdBy;

  @Column(name = "updated_by")
  private UUID updatedBy;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "is_deleted", nullable = false)
  private boolean isDeleted;
}
