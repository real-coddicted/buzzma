-- ============================================================
-- CONNECTION MODULE
-- ============================================================

CREATE TABLE connections (
    id            uuid                      NOT NULL,
    from_user_id  uuid                      NOT NULL,
    to_user_id    uuid                      NOT NULL,
    status        varchar(50)               NOT NULL,
    created_by    uuid,
    updated_by    uuid,
    created_at    timestamp with time zone  NOT NULL,
    updated_at    timestamp with time zone  NOT NULL,
    is_deleted    boolean                   NOT NULL DEFAULT false,
    CONSTRAINT pk_connections           PRIMARY KEY (id),
    CONSTRAINT fk_connections_from_user FOREIGN KEY (from_user_id) REFERENCES users (id),
    CONSTRAINT fk_connections_to_user   FOREIGN KEY (to_user_id)   REFERENCES users (id)
);

CREATE INDEX idx_connections_from_user_id ON connections (from_user_id);
CREATE INDEX idx_connections_to_user_id   ON connections (to_user_id);
CREATE INDEX idx_connections_is_deleted   ON connections (is_deleted);
