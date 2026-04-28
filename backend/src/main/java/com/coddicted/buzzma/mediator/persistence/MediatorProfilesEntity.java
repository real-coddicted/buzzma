package com.coddicted.buzzma.mediator.persistence;

import com.coddicted.buzzma.shared.common.AuditEntityListener;
import com.coddicted.buzzma.shared.common.Auditable;
import com.coddicted.buzzma.shared.enums.MediatorStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "mediator_profiles")
@EntityListeners(AuditEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class MediatorProfilesEntity implements Auditable {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "user_id", unique = true, nullable = false)
  private UUID userId;

  @Column(name = "mediator_code", unique = true, nullable = false)
  private String mediatorCode;

  @Column(name = "parent_agency_code")
  private String parentAgencyCode;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private MediatorStatus status = MediatorStatus.active;

  @Column(name = "created_by")
  private UUID createdBy;

  @Column(name = "updated_by")
  private UUID updatedBy;

  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted = false;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
