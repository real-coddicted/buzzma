package com.coddicted.buzzma.identity.entity;

import com.coddicted.buzzma.shared.common.AuditEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

import lombok.*;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "user_banking_details")
@EntityListeners(AuditEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserBankingDetail {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "account_number")
  private String accountNumber;

  @Column(name = "ifsc_code")
  private String bankIfscCode;

  @Column(name = "bank_name")
  private String bankName;

  @Column(name = "account_holder_name")
  private String accountHolderName;

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
