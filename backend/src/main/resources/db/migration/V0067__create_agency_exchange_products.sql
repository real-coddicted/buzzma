-- Phase 1 (Exchange Deals, ref #726 / parent #382): schema-only groundwork.
-- Per-agency master list of exchange product names. A dedicated table rather
-- than reusing `products`: `products` is campaign-scoped (one row per campaign,
-- carries image_url / product_link / price_paise, FK'd from campaigns.product_id)
-- and is the wrong grain for a lightweight per-agency name list.
--
-- agency_id is the users.id of a ROLE_AGENCY user - there is no separate
-- agencies table. No FK to users is added, consistent with how
-- campaigns.owner_id / claims.owner_id are left unconstrained in this codebase.
-- Table-level GRANTs are applied automatically by afterMigrate.sql.
-- No read/write path touches this table in Phase 1.
CREATE TABLE IF NOT EXISTS agency_exchange_products (
    id          uuid         NOT NULL,
    agency_id   uuid         NOT NULL,
    name        varchar(255) NOT NULL,
    created_by  uuid,
    updated_by  uuid,
    created_at  timestamp with time zone NOT NULL,
    updated_at  timestamp with time zone NOT NULL,
    is_deleted  boolean      NOT NULL DEFAULT false,
    CONSTRAINT pk_agency_exchange_products PRIMARY KEY (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_agency_exchange_products_agency_name
    ON agency_exchange_products (agency_id, lower(name)) WHERE is_deleted = false;

CREATE INDEX IF NOT EXISTS idx_agency_exchange_products_agency
    ON agency_exchange_products (agency_id) WHERE is_deleted = false;
