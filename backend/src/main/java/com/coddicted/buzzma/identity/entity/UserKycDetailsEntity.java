package com.coddicted.buzzma.identity.entity;

import com.coddicted.buzzma.shared.enums.KycStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

public class UserKycDetailsEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private KycStatus status = KycStatus.none;

    @Column(name = "pan_card")
    private String panCard;

    @Column(name = "aadhaar")
    private String aadhaar;

    @Column(name = "gst")
    private String gst;
}
