package com.coddicted.buzzma.support.persistence;

import com.coddicted.buzzma.shared.common.AuditEntityListener;
import com.coddicted.buzzma.shared.common.Auditable;
import com.coddicted.buzzma.shared.enums.TicketStatus;
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
@Table(name = "tickets")
@EntityListeners(AuditEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class TicketsEntity implements Auditable {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "user_name", nullable = false)
  private String userName;

  @Column(name = "role", nullable = false)
  private String role;

  @Column(name = "order_id")
  private String orderId;

  @Column(name = "issue_type", nullable = false)
  private String issueType;

  @Column(name = "description", nullable = false)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private TicketStatus status = TicketStatus.Open;

  @Column(name = "target_role")
  private String targetRole;

  @Column(name = "priority")
  private String priority = "medium";

  @Column(name = "resolved_by")
  private UUID resolvedBy;

  @Column(name = "resolved_at")
  private Instant resolvedAt;

  @Column(name = "resolution_note", length = 1000)
  private String resolutionNote;

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
