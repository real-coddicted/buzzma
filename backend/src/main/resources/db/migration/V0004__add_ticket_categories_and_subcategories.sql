INSERT INTO ticket_categories (
    id,
    name,
    code,
    created_by,
    updated_by,
    created_at,
    updated_at,
    is_deleted
) VALUES
    ('f1fd9d09-4dbe-4dab-bde1-80f50f1df3ba', 'Technical', 'TICKET_CATEGORY_TECHNICAL', NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
    ('7ef3c07f-f99c-4f6c-a052-7c8f2b07295b', 'Claim', 'TICKET_CATEGORY_CLAIM', NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false);

INSERT INTO ticket_sub_categories (
    id,
    category_id,
    name,
    code,
    metadata,
    created_by,
    updated_by,
    created_at,
    updated_at,
    deleted
) VALUES
    ('8a3f6e56-95da-4cff-bf5b-d0898b569ecb', 'f1fd9d09-4dbe-4dab-bde1-80f50f1df3ba', 'General', 'TICKET_SUB_CATEGORY_GENERAL', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
    ('76a18ca2-29fc-4dc1-b078-a310b7083f1e', '7ef3c07f-f99c-4f6c-a052-7c8f2b07295b', 'Cashback Delay', 'TICKET_SUB_CATEGORY_CASHBACK_DELAY', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
    ('7ee64030-76cf-4719-b7d4-4d66b429e4f6', '7ef3c07f-f99c-4f6c-a052-7c8f2b07295b', 'Wrong Amount', 'TICKET_SUB_CATEGORY_WRONG_AMOUNT', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
    ('373ca572-5cc0-4462-9f88-58cc57ae6528', '7ef3c07f-f99c-4f6c-a052-7c8f2b07295b', 'Product Issue', 'TICKET_SUB_CATEGORY_PRODUCT_ISSUE', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
    ('e3f0fbb2-59ff-4478-a3ca-0db5fe946e4f', '7ef3c07f-f99c-4f6c-a052-7c8f2b07295b', 'Delivery Problem', 'TICKET_SUB_CATEGORY_DELIVERY_PROBLEM', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
    ('9e174cae-4c6c-43f8-a0f0-84df5c4ce272', '7ef3c07f-f99c-4f6c-a052-7c8f2b07295b', 'Refund Request', 'TICKET_SUB_CATEGORY_REFUND_REQUEST', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
    ('87f0cf8d-b4fd-4bca-a3f5-40cddf20ae3c', '7ef3c07f-f99c-4f6c-a052-7c8f2b07295b', 'Other', 'TICKET_SUB_CATEGORY_OTHER', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false);

