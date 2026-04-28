package com.coddicted.buzzma.notifications.persistence;

import com.coddicted.buzzma.shared.enums.PushApp;
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
@Table(name = "push_subscriptions")
@Getter
@Setter
@NoArgsConstructor
public class PushSubscriptionsEntity {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Enumerated(EnumType.STRING)
  @Column(name = "app", nullable = false)
  private PushApp app;

  @Column(name = "endpoint", unique = true, nullable = false)
  private String endpoint;

  @Column(name = "expiration_time")
  private Integer expirationTime;

  @Column(name = "keys_p256dh", nullable = false)
  private String keysP256dh;

  @Column(name = "keys_auth", nullable = false)
  private String keysAuth;

  @Column(name = "user_agent")
  private String userAgent;

  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted = false;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();
}
