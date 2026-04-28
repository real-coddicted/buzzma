package com.coddicted.buzzma.mediator.persistence;

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
@Table(name = "pending_connections")
@Getter
@Setter
@NoArgsConstructor
public class PendingConnectionsEntity {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "agency_id")
  private String agencyId;

  @Column(name = "agency_name")
  private String agencyName;

  @Column(name = "agency_code")
  private String agencyCode;

  @Column(name = "timestamp")
  private Instant timestamp = Instant.now();

  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted = false;
}
