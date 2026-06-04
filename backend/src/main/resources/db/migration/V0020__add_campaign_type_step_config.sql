CREATE TABLE campaign_type_step (
    campaign_type VARCHAR(50) NOT NULL,
    step_type     VARCHAR(50) NOT NULL,
    step_order    INTEGER     NOT NULL,
    PRIMARY KEY (campaign_type, step_type)
);

INSERT INTO campaign_type_step (campaign_type, step_type, step_order) VALUES
('CAMPAIGN_TYPE_ORDER',    'ORDER',         1),
('CAMPAIGN_TYPE_ORDER',    'RETURN_WINDOW', 2),
('CAMPAIGN_TYPE_ORDER',    'CASHBACK',      3),
('CAMPAIGN_TYPE_RATING',   'ORDER',         1),
('CAMPAIGN_TYPE_RATING',   'RATING',        2),
('CAMPAIGN_TYPE_RATING',   'RETURN_WINDOW', 3),
('CAMPAIGN_TYPE_RATING',   'CASHBACK',      4),
('CAMPAIGN_TYPE_REVIEW',   'ORDER',         1),
('CAMPAIGN_TYPE_REVIEW',   'RATING',        2),
('CAMPAIGN_TYPE_REVIEW',   'REVIEW',        3),
('CAMPAIGN_TYPE_REVIEW',   'RETURN_WINDOW', 4),
('CAMPAIGN_TYPE_REVIEW',   'CASHBACK',      5),
('CAMPAIGN_TYPE_DISCOUNT', 'ORDER',         1),
('CAMPAIGN_TYPE_DISCOUNT', 'RETURN_WINDOW', 2),
('CAMPAIGN_TYPE_DISCOUNT', 'CASHBACK',      3);
