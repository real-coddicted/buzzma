ALTER TABLE claims
    ALTER COLUMN score TYPE integer USING ROUND(score)::integer;
