ALTER TABLE claims
    ALTER COLUMN order_date TYPE integer USING (order_date::integer);
