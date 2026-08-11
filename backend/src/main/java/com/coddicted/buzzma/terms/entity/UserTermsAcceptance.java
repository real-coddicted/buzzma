package com.coddicted.buzzma.terms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "user_terms_acceptance")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTermsAcceptance {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "user_id", nullable = false, updatable = false)
  private UUID userId;

  @Column(name = "terms_version", nullable = false, updatable = false)
  private String termsVersion;

  @Column(name = "accepted_at", nullable = false, updatable = false)
  private Instant acceptedAt;
}
