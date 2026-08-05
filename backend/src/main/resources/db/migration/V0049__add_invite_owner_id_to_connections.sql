ALTER TABLE connections ADD COLUMN invite_owner_id uuid;

UPDATE connections SET invite_owner_id = from_user_id WHERE invite_owner_id IS NULL;

ALTER TABLE connections ALTER COLUMN invite_owner_id SET NOT NULL;