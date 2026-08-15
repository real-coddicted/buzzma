CREATE TABLE email_communication_log (
    id             uuid                     NOT NULL,
    to_address     varchar(320)             NOT NULL,
    from_address   varchar(320)             NOT NULL,
    subject        varchar(998)             NOT NULL,
    status         varchar(32)              NOT NULL,
    sent_at        timestamp with time zone,
    error_message  text,
    created_by     uuid,
    updated_by     uuid,
    created_at     timestamp with time zone NOT NULL,
    updated_at     timestamp with time zone NOT NULL,
    CONSTRAINT pk_email_communication_log PRIMARY KEY (id)
);
CREATE INDEX idx_email_communication_log_created_at ON email_communication_log (created_at);