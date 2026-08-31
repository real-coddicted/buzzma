-- Convert products.image_url (single text) to products.image_urls (jsonb array).
-- Phase 1 of multi-image support: storage only, no behaviour change — every read
-- path continues to expose the first array element as the single productImageUrl.

ALTER TABLE products ADD COLUMN image_urls jsonb;

UPDATE products SET image_urls = jsonb_build_array(image_url);

ALTER TABLE products ALTER COLUMN image_urls SET NOT NULL;

ALTER TABLE products DROP COLUMN image_url;
