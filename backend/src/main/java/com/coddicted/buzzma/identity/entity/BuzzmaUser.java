package com.coddicted.buzzma.identity.entity;

import com.coddicted.buzzma.shared.common.AuditEntityListener;
import com.coddicted.buzzma.shared.common.Auditable;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "users")
@EntityListeners(AuditEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class BuzzmaUser implements Auditable {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "name", length = 120, nullable = false)
  private String name;

  @Column(name = "username", length = 64, unique = true)
  private String username;

  @Column(name = "mobile", length = 10, nullable = false)
  private String mobile;

  @Column(name = "email", length = 320)
  private String email;

  @Enumerated(EnumType.STRING)
  @Column(name = "role")
  private UserRole role;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private UserStatus status;

  // Audit fields
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
