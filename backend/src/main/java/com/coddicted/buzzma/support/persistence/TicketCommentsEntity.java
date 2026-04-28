package com.coddicted.buzzma.support.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "ticket_comments")
@Getter
@Setter
@NoArgsConstructor
public class TicketCommentsEntity {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "ticket_id", nullable = false)
  private UUID ticketId;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "user_name", nullable = false)
  private String userName;

  @Column(name = "role", nullable = false)
  private String role;

  @Column(name = "message", length = 2000, nullable = false)
  private String message;

  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted = false;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();
}
