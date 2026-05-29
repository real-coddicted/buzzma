-- Add brand_name to products

ALTER TABLE products ADD COLUMN brand_name varchar(255);

-- Backfill existing rows with empty string and enforce NOT NULL to match entity
UPDATE products SET brand_name = '' WHERE brand_name IS NULL;

ALTER TABLE products ALTER COLUMN brand_name SET NOT NULL;

-- No DB default; application will set brand on create

