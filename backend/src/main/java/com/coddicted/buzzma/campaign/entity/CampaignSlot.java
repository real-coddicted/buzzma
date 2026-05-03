package com.coddicted.buzzma.campaign.entity;

import com.coddicted.buzzma.shared.common.AuditEntityListener;
import com.coddicted.buzzma.shared.common.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
@Table(name = "campaign_slots")
@EntityListeners(AuditEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class CampaignSlot implements Auditable {

  @Id @GeneratedValue @UuidGenerator private UUID id;

  @Column(name = "campaign_id", nullable = false)
  private UUID campaignId;

  @Column(name = "total_slots", nullable = false)
  private Integer totalSlots;

  /*
  Rationale of having slots available vs slots used:
  slots available needs to be displayed to potentially thousands of buyers
  whereas slots used needs to be displayed only to agency/ mediators who would be
  much lesser in number compared to buyers. Hence, lesser computation requirement.

  Start value would be same as totalSlots
  */
  @Column(name = "slots_available", nullable = false)
  private Integer slotsAvailable;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "created_by", nullable = false, updatable = false)
  private UUID createdBy;

  @Column(name = "updated_by")
  private UUID updatedBy;

  @Column(name = "is_deleted")
  private boolean isDeleted;
}
