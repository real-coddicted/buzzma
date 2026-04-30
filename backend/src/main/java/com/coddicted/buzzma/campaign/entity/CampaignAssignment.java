package com.coddicted.buzzma.campaign.entity;

import com.coddicted.buzzma.shared.common.AuditEntityListener;
import com.coddicted.buzzma.shared.common.Auditable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "campaign_assignments")
@EntityListeners(AuditEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CampaignAssignment implements Auditable {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "campaign_id", nullable = false)
    private UUID campaignId;

    @Column(name = "assignor_id", nullable=false)
    private UUID assignorId;

    @Column(name = "assignee_id", nullable = false)
    private UUID assigneeId;   // agencyCode or mediatorCode

    @Column(name = "slot_limit", nullable = false)
    private Integer slotLimit;

    @Column(name = "campaign_price_paise", nullable = false)
    private BigInteger campaignPricePaise;

    // commission in paise that the assignee will get for each successful conversion tracked for this campaign assignment
    @Column(name = "commission_offered_paise")
    private BigInteger commissionOfferedPaise;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private CampaignAssignmentStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "is_deleted")
    private boolean isDeleted;
}

