package com.coddicted.buzzma.identity.entity;

import com.coddicted.buzzma.shared.enums.InviteStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "invites")
@Getter
@Setter
@NoArgsConstructor
public class InviteEntity {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "code", unique = true, nullable = false)
  private String code;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  private UserRole role;

  @Column(name = "label")
  private String label;

  @Column(name = "parent_user_id")
  private UUID parentUserId;

  @Column(name = "parent_code")
  private String parentCode;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private InviteStatus status = InviteStatus.active;

  @Column(name = "max_uses")
  private Integer maxUses = 1;

  @Column(name = "use_count")
  private Integer useCount = 0;

  @Column(name = "expires_at")
  private Instant expiresAt;

  @Column(name = "created_by")
  private UUID createdBy;

  @Column(name = "used_by")
  private UUID usedBy;

  @Column(name = "used_at")
  private Instant usedAt;

  @Column(name = "uses", columnDefinition = "jsonb")
  private String uses = "[]";

  @Column(name = "revoked_by")
  private UUID revokedBy;

  @Column(name = "revoked_at")
  private Instant revokedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();
}
