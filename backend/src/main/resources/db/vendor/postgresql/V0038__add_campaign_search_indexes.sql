CREATE INDEX idx_campaigns_type ON campaigns (type);
CREATE INDEX idx_campaigns_start_date ON campaigns (start_date);
CREATE INDEX idx_products_brand_name ON products (LOWER(brand_name));