package com.coddicted.buzzma.campaign.entity;

import com.coddicted.buzzma.shared.common.AuditEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigInteger;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "commissions")
@EntityListeners(AuditEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Commission {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "campaign_id", nullable = false)
  private UUID campaignId;

  @Column(name = "charged_by_id")
  private UUID chargedById;

  @Column(name = "commission")
  private BigInteger commissionPaise;
}
