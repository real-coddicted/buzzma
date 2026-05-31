CREATE TABLE notifications (
    id         uuid                     NOT NULL,
    user_id    uuid                     NOT NULL,
    status     varchar(50),
    payload    jsonb,
    is_pinned  boolean                  NOT NULL DEFAULT false,
    created_by uuid,
    updated_by uuid,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    is_deleted boolean                  NOT NULL DEFAULT false,
    CONSTRAINT pk_notifications PRIMARY KEY (id),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_notifications_user_id ON notifications (user_id);
CREATE INDEX idx_notifications_is_deleted ON notifications (is_deleted);
