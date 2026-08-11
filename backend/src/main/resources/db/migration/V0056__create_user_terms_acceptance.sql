CREATE TABLE user_terms_acceptance (
    id             uuid         NOT NULL,
    user_id        uuid         NOT NULL,
    terms_version  varchar(32)  NOT NULL,
    accepted_at    timestamp with time zone  NOT NULL,
    CONSTRAINT pk_user_terms_acceptance          PRIMARY KEY (id),
    CONSTRAINT fk_user_terms_acceptance_user     FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_user_terms_acceptance_user_id ON user_terms_acceptance (user_id);