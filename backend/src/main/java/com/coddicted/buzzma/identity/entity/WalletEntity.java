package com.coddicted.buzzma.identity.entity;

import com.coddicted.buzzma.shared.common.AuditEntityListener;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "wallets")
@EntityListeners(AuditEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class WalletEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "balance_paise")
    private Integer balancePaise = 0;

    @Column(name = "pending_amount_paise")
    private Integer pendingAmountPaise = 0;
}
