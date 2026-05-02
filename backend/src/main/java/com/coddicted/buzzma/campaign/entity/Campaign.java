package com.coddicted.buzzma.campaign.entity;

import com.coddicted.buzzma.shared.common.AuditEntityListener;
import com.coddicted.buzzma.shared.common.Auditable;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "campaigns")
@EntityListeners(AuditEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Campaign implements Auditable {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "title", length = 200, nullable = false)
  private String title;

  @Column(name = "owner_id", nullable = false)
  private UUID ownerId;

  @Column(name = "total_slots", nullable = false)
  private Integer totalSlots;

  // one product one campaign
  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false)
  private Product product;

  @Enumerated(EnumType.STRING)
  @Column(name = "type")
  private CampaignType type;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private CampaignStatus status;

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
