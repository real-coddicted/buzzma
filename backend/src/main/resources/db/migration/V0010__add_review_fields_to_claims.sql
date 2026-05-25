ALTER TABLE claims RENAME COLUMN overall_verified TO mediator_verified;
ALTER TABLE claims RENAME COLUMN rejection_note   TO reviewer_comments;
ALTER TABLE claims RENAME COLUMN overall_score     TO score;
ALTER TABLE claims ALTER  COLUMN score TYPE NUMERIC USING score::numeric;
ALTER TABLE claims DROP   COLUMN IF EXISTS comments;

ALTER TABLE claims ADD COLUMN reviewer_id   uuid;
ALTER TABLE claims ADD COLUMN review_status varchar(50);