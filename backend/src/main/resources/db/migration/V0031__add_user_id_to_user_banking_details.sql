ALTER TABLE user_banking_details
    ADD COLUMN user_id uuid NOT NULL REFERENCES users (id);

ALTER TABLE user_banking_details
    ADD CONSTRAINT uq_user_banking_details_user_id UNIQUE (user_id);