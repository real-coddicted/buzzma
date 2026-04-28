package com.coddicted.buzzma.campaign.entity;

import com.coddicted.buzzma.shared.common.AuditEntityListener;
import com.coddicted.buzzma.shared.common.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
public class CampaignAssignment implements Auditable {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name= "parent_id")
    private UUID parentId;

    @Column(name = "campaign_id", nullable = false)
    private UUID campaignId;

    @Enumerated(EnumType.STRING)
    @Column(name = "assigned_to_type", nullable = false)
    private AssigneeType assignedToType;  // AGENCY | MEDIATOR

    // human-readable unique code for agency or mediator to which the campaign is assigned.
    // This will be used by the tracking system to attribute conversions to the correct assignee.
    @Column(name = "assigned_to_code", nullable = false)
    private String assignedToCode;   // agencyCode or mediatorCode

    @Column(name = "slot_limit", nullable = false)
    private Integer slotLimit;

    // commission in paise that the assignee will get for each successful conversion tracked for this campaign assignment
    @Column(name = "commission_offered_paise")
    private BigInteger commissionOfferedPaise;

    // commission in paise that the assignee has charged for each successful conversion tracked for this campaign assignment.
    @Column(name = "commission_charged_paise")
    private BigInteger commissionChargedPaise;

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

