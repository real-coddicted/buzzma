package com.coddicted.buzzma.identity.entity;

import com.coddicted.buzzma.shared.common.AuditEntityListener;
import com.coddicted.buzzma.shared.common.Auditable;
import com.coddicted.buzzma.shared.enums.KycStatus;
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
@Table(name = "users")
@EntityListeners(AuditEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class UsersEntity implements Auditable {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "first_name", length = 120, nullable = false)
  private String firstName;

  @Column(name = "last_name", length = 120, nullable = false)
  private String lastName;

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

  @Column(name = "mediator_code", length = 64)
  private String mediatorCode;

  @Column(name = "parent_code", length = 64)
  private String parentCode;

  @Column(name = "upi_id")
  private String upiId;

  @Column(name = "qr_code")
  private String qrCode;

  @Column(name = "avatar")
  private String avatar;

  @Column(name = "failed_login_attempts")
  private Integer failedLoginAttempts = 0;

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
