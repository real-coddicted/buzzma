package com.coddicted.buzzma.campaign.entity;

import com.coddicted.buzzma.shared.common.AuditEntityListener;
import com.coddicted.buzzma.shared.common.Auditable;
import com.coddicted.buzzma.shared.enums.DealType;
import jakarta.persistence.*;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "deals")
@EntityListeners(AuditEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Deal implements Auditable {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "campaign_id", nullable = false)
  private UUID campaignId;

  @Column(name = "mediator_code", nullable = false)
  private String mediatorCode;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "description")
  private String description = "Exclusive";

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false)
  private Product product;

  @Column(name = "deal_price_paise", nullable = false)
  private BigInteger dealPricePaise;

  @Enumerated(EnumType.STRING)
  @Column(name = "deal_type", nullable = false)
  private DealType dealType;

  @Enumerated(EnumType.STRING)
  @Column(name = "deal_status", nullable = false)
  private DealStatus status;

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
