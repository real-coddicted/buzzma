CREATE TABLE payments (
    id                     UUID        NOT NULL DEFAULT gen_random_uuid(),
    payer_id               UUID        NOT NULL,
    payee_id               UUID        NOT NULL,

    screenshot_storage_key VARCHAR(500),
    amount_paid_paise      NUMERIC     NOT NULL,
    payment_method         VARCHAR(20) NOT NULL,
    utr_ref                VARCHAR(100),
    notes                  TEXT,
    paid_at                TIMESTAMPTZ NOT NULL,

    created_by             UUID,
    updated_by             UUID,
    created_at             TIMESTAMPTZ NOT NULL,
    updated_at             TIMESTAMPTZ NOT NULL,

    PRIMARY KEY (id)
);

CREATE INDEX idx_payments_payer_id ON payments (payer_id);
CREATE INDEX idx_payments_payee_id ON payments (payee_id);

ALTER TABLE claim_accountings
    ADD CONSTRAINT fk_claim_accountings_mediator_payment
        FOREIGN KEY (mediator_payment_id) REFERENCES payments(id);

ALTER TABLE claim_accountings
    ADD CONSTRAINT fk_claim_accountings_buyer_payment
        FOREIGN KEY (buyer_payment_id) REFERENCES payments(id);
