package com.coddicted.buzzma.campaign.entity;

import com.coddicted.buzzma.shared.common.AuditEntityListener;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigInteger;
import java.util.UUID;

@Entity
@Table(name = "campaigns")
@EntityListeners(AuditEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Commissions {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignee_type", nullable = false)
    private AssigneeType assigneeType;  // AGENCY | MEDIATOR

    // human-readable unique code for agency or mediator to which the campaign is assigned.
    // This will be used by the tracking system to attribute conversions to the correct assignee.
    @Column(name = "assignee_code", nullable = false)
    private String assigneeCode;   // agencyCode or mediatorCode

    @Column(name = "commission")
    private BigInteger commissionPaise;
}
