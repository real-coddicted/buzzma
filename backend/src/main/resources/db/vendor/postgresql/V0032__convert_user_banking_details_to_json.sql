ALTER TABLE user_banking_details
    ADD COLUMN bank_details jsonb;

UPDATE user_banking_details
SET bank_details = jsonb_build_object(
    'accountNumber', account_number,
    'bankIfscCode', ifsc_code,
    'bankName', bank_name,
    'accountHolderName', account_holder_name);

ALTER TABLE user_banking_details DROP COLUMN account_number;
ALTER TABLE user_banking_details DROP COLUMN ifsc_code;
ALTER TABLE user_banking_details DROP COLUMN bank_name;
ALTER TABLE user_banking_details DROP COLUMN account_holder_name;