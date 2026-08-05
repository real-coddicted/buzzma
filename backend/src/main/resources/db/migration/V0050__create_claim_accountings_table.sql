CREATE TABLE claim_accountings (
    id                        UUID        NOT NULL DEFAULT gen_random_uuid(),
    claim_id                  UUID        NOT NULL,
    campaign_id               UUID        NOT NULL,
    deal_id                   UUID        NOT NULL,

    buyer_id                  UUID        NOT NULL,
    mediator_id               UUID        NOT NULL,
    agency_id                 UUID        NOT NULL,

    mediator_receivable_paise NUMERIC     NOT NULL,
    buyer_receivable_paise    NUMERIC     NOT NULL,

    mediator_payment_status   VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    mediator_paid_at          TIMESTAMPTZ,
    mediator_payment_id       UUID,

    buyer_payment_status      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    buyer_paid_at             TIMESTAMPTZ,
    buyer_payment_id          UUID,

    created_by                UUID,
    updated_by                UUID,
    created_at                TIMESTAMPTZ NOT NULL,
    updated_at                TIMESTAMPTZ NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uq_claim_accountings_claim UNIQUE (claim_id),
    CONSTRAINT fk_claim_accountings_claim FOREIGN KEY (claim_id) REFERENCES claims(id)
);

CREATE INDEX idx_claim_accountings_buyer_id    ON claim_accountings (buyer_id);
CREATE INDEX idx_claim_accountings_mediator_id ON claim_accountings (mediator_id);
CREATE INDEX idx_claim_accountings_agency_id   ON claim_accountings (agency_id);
CREATE INDEX idx_claim_accountings_campaign_id ON claim_accountings (campaign_id);
