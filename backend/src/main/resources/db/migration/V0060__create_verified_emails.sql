CREATE TABLE verified_emails (
    id           uuid                     NOT NULL,
    user_id      uuid                     NOT NULL,
    email        varchar(320)             NOT NULL,
    full_name    varchar(120)             NOT NULL,
    created_by   uuid,
    updated_by   uuid,
    created_at   timestamp with time zone NOT NULL,
    updated_at   timestamp with time zone NOT NULL,
    CONSTRAINT pk_verified_emails PRIMARY KEY (id),
    CONSTRAINT fk_verified_emails_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_verified_emails_user_id ON verified_emails(user_id);
CREATE INDEX idx_verified_emails_email ON verified_emails(email);