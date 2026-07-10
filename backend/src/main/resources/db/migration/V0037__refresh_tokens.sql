CREATE TABLE refresh_tokens (
    id           uuid                     NOT NULL,
    user_id      uuid                     NOT NULL,
    token_hash   varchar(64)              NOT NULL,
    expires_at   timestamp with time zone NOT NULL,
    created_at   timestamp with time zone NOT NULL,
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);
CREATE        INDEX idx_refresh_tokens_user_id    ON refresh_tokens(user_id);
