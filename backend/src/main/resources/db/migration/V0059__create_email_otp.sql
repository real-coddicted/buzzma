CREATE TABLE email_otp (
    id           uuid                     NOT NULL,
    user_id      uuid                     NOT NULL,
    otp_hash     varchar(64)              NOT NULL,
    expires_at   timestamp with time zone NOT NULL,
    is_used      boolean                  NOT NULL DEFAULT false,
    created_by   uuid,
    updated_by   uuid,
    created_at   timestamp with time zone NOT NULL,
    updated_at   timestamp with time zone NOT NULL,
    CONSTRAINT pk_email_otp PRIMARY KEY (id),
    CONSTRAINT fk_email_otp_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_email_otp_user_id ON email_otp(user_id);