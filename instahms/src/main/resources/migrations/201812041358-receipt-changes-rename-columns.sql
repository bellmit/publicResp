-- liquibase formatted sql
-- changeset allabakash:Receipt-changes-rename-columns 

ALTER TABLE bill_receipts RENAME COLUMN recpt_type TO recpt_type_obsolete;
ALTER TABLE bill_receipts RENAME COLUMN payment_type TO payment_type_obsolete;
ALTER TABLE bill_receipts RENAME COLUMN counter TO counter_obsolete; 
ALTER TABLE bill_receipts RENAME COLUMN bank_name TO bank_name_obsolete; 
ALTER TABLE bill_receipts RENAME COLUMN reference_no TO reference_no_obsolete; 
ALTER TABLE bill_receipts RENAME COLUMN remarks TO remarks_obsolete; 
ALTER TABLE bill_receipts RENAME COLUMN payment_mode_id TO payment_mode_id_obsolete; 
ALTER TABLE bill_receipts RENAME COLUMN card_type_id TO card_type_id_obsolete; 
ALTER TABLE bill_receipts RENAME COLUMN bank_batch_no TO bank_batch_no_obsolete; 
ALTER TABLE bill_receipts RENAME COLUMN card_auth_code TO card_auth_code_obsolete; 
ALTER TABLE bill_receipts RENAME COLUMN card_holder_name TO card_holder_name_obsolete; 
ALTER TABLE bill_receipts RENAME COLUMN currency_id TO currency_id_obsolete; 
ALTER TABLE bill_receipts RENAME COLUMN exchange_date TO exchange_date_obsolete; 
ALTER TABLE bill_receipts RENAME COLUMN exchange_rate TO exchange_rate_obsolete; 
ALTER TABLE bill_receipts RENAME COLUMN currency_amt TO currency_amt_obsolete; 
ALTER TABLE bill_receipts RENAME COLUMN card_expdate TO card_expdate_obsolete; 
ALTER TABLE bill_receipts RENAME COLUMN card_number TO card_number_obsolete; 
ALTER TABLE bill_receipts RENAME COLUMN credit_card_commission_percentage TO credit_card_commission_percentage_obsolete; 
ALTER TABLE bill_receipts RENAME COLUMN credit_card_commission_amount TO credit_card_commission_amount_obsolete; 
ALTER TABLE bill_receipts RENAME COLUMN totp TO totp_obsolete; 
ALTER TABLE bill_receipts RENAME COLUMN edc_imei TO edc_imei_obsolete; 
ALTER TABLE bill_receipts RENAME COLUMN mob_number TO mob_number_obsolete;
ALTER TABLE bill_receipts RENAME COLUMN tds_amt TO tds_amt_obsolete;
ALTER TABLE bill_receipts RENAME COLUMN paid_by TO paid_by_obsolete;
ALTER TABLE bill_receipts RENAME COLUMN amount TO amount_obsolete;

ALTER TABLE patient_deposits RENAME COLUMN deposit_no to deposit_no_absolete;
ALTER TABLE patient_deposits RENAME COLUMN mr_no to mr_no_absolete;
ALTER TABLE patient_deposits RENAME COLUMN deposit_type to deposit_type_absolete;
ALTER TABLE patient_deposits RENAME COLUMN amount to amount_absolete;
ALTER TABLE patient_deposits RENAME COLUMN deposit_available_for to deposit_available_for_absolete;
ALTER TABLE patient_deposits RENAME COLUMN package_id to package_id_absolete;

