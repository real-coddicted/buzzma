ALTER TABLE claim_screenshots
    ALTER COLUMN score TYPE integer USING ROUND(score * 100)::integer;
