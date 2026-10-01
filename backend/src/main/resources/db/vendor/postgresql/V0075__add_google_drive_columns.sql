ALTER TABLE claim_screenshots ADD COLUMN google_drive_url VARCHAR(1000);

ALTER TABLE claims ADD COLUMN google_drive_folder_id VARCHAR(200);
