package com.coddicted.buzzma.extraction.entity;

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
import java.util.List;
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
@Table(name = "extraction_jobs")
@EntityListeners(AuditEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ExtractionJob implements Auditable {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "submitted_by", nullable = false)
  private UUID submittedBy;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 50)
  private ExtractionJobStatus status;

  @Column(name = "storage_key", length = 500)
  private String storageKey;

  @Column(name = "original_filename", length = 255)
  private String originalFilename;

  @Column(name = "content_type", length = 100)
  private String contentType;

  @Column(name = "attempt_count", nullable = false)
  private int attemptCount;

  @Column(name = "error_message", columnDefinition = "TEXT")
  private String errorMessage;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "result", columnDefinition = "jsonb")
  private ExtractionResult result;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "validation_errors", columnDefinition = "jsonb")
  private List<ValidationError> validationErrors;

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
