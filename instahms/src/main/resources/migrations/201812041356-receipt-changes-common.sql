-- liquibase formatted sql
-- changeset qwewrty1:Receipt-changes-common

CREATE TABLE receipt_usage (
  receipt_id VARCHAR(15),
  entity_type VARCHAR(25),
  entity_id VARCHAR(15),
  PRIMARY KEY(receipt_id, entity_id)
);
COMMENT ON TABLE receipt_usage IS '{ "type": "Txn", "comment": "Holds restrictions against receipts" }';

--- updating the sponsor_ids in bill_receipts table where sponsor_id is blank and payment type is sponsor.
--- primary sponsor receipts

UPDATE bill_receipts br SET sponsor_id = pr.primary_sponsor_id FROM patient_registration pr JOIN bill b ON b.visit_id=pr.patient_id
WHERE  br.bill_no=b.bill_no AND br.sponsor_index='P' AND br.sponsor_id IS null AND br.payment_type='S';

--- secondary sponsor receipts

UPDATE bill_receipts br SET sponsor_id = pr.secondary_sponsor_id FROM patient_registration pr JOIN bill b ON b.visit_id=pr.patient_id
WHERE  br.bill_no=b.bill_no AND br.sponsor_index='S' AND br.sponsor_id IS null AND br.payment_type='S';


---- Migrating data from bill_receipts to receipts table
 
INSERT INTO receipts SELECT receipt_no, CASE WHEN payment_type='F' THEN 'F' else 'R' END AS recipt_type, sponsor_id, 
 (SELECT mr_no FROM patient_registration pr INNER JOIN bill b ON pr.patient_id = b.visit_id AND b.bill_no = br.bill_no LIMIT 1) AS mr_no,
 null, amount,display_date, counter,bank_name, reference_no, username, mod_time,username, mod_time, remarks, tds_amt, paid_by, 
 payment_mode_id, card_type_id, bank_batch_no,card_auth_code, card_holder_name , currency_id, exchange_rate, exchange_date, 
 currency_amt, card_expdate, card_number, credit_card_commission_percentage, credit_card_commission_amount,totp ,mob_number, 
 edc_imei, 0, display_date, null, CASE WHEN recpt_type='A' THEN false ELSE true END AS settlement,(amount+tds_amt),'N',false,null,null,null,0 FROM bill_receipts br;

----Mapping bill_no and advance receipts

INSERT INTO receipt_usage SELECT receipt_no, 'bill_no' AS entity_type, bill_no AS entity_id FROM bill_receipts;

ALTER TABLE bill_receipts ALTER COLUMN recpt_type DROP NOT NULL;
ALTER TABLE bill_receipts ALTER COLUMN payment_type DROP NOT NULL;
ALTER TABLE bill_receipts ALTER COLUMN payment_mode_id DROP NOT NULL;
ALTER TABLE bill_receipts ALTER COLUMN amount DROP NOT NULL;

INSERT INTO payment_mode_master (mode_id,payment_mode,card_type_required,bank_required,ref_required,
 realization_required,status,displayorder,spl_account_name,bank_batch_required,card_auth_required,
 card_holder_required,card_number_required,card_expdate_required,totp_required,
 mobile_number_required,transaction_limit,allow_payments_more_than_transaction_limit) VALUES(-6, 
 'General Deposit', 'N', 'N', 'N', 'N', 'A', 8, '${center_code}-General-Deposit', 'N', 'N', 'N', 
 'N', 'N', 'N', 'N', 0.00, 'A');
INSERT INTO payment_mode_master (mode_id,payment_mode,card_type_required,bank_required,ref_required,
 realization_required,status,displayorder,spl_account_name,bank_batch_required,card_auth_required,
 card_holder_required,card_number_required,card_expdate_required,totp_required,
 mobile_number_required,transaction_limit,allow_payments_more_than_transaction_limit) VALUES(-7, 
 'IP Deposit', 'N', 'N', 'N', 'N', 'A', 8, '${center_code}-IP-Deposit', 'N', 'N', 'N', 'N', 'N', 
 'N', 'N', 0.00, 'A');
INSERT INTO payment_mode_master (mode_id,payment_mode,card_type_required,bank_required,ref_required,
 realization_required,status,displayorder,spl_account_name,bank_batch_required,card_auth_required,
 card_holder_required,card_number_required,card_expdate_required,totp_required,
 mobile_number_required,transaction_limit,allow_payments_more_than_transaction_limit) VALUES(-8, 
 'Package Deposit', 'N', 'N', 'N', 'N', 'A', 8, '${center_code}-Package-Deposit', 'N', 'N', 'N', 
 'N', 'N', 'N', 'N', 0.00, 'A');
INSERT INTO payment_mode_master (mode_id,payment_mode,card_type_required,bank_required,ref_required,
 realization_required,status,displayorder,spl_account_name,bank_batch_required,card_auth_required,
 card_holder_required,card_number_required,card_expdate_required,totp_required,
 mobile_number_required,transaction_limit,allow_payments_more_than_transaction_limit) VALUES(-9, 
 'Reward Points', 'N', 'N', 'N', 'N', 'A', 8, '${center_code}-Redeem-Points', 'N', 'N', 'N', 'N', 
 'N', 'N', 'N', 0.00, 'A');

---- Migrating data from bill_receipts to receipts table
 
INSERT INTO receipts SELECT deposit_no, deposit_type, NULL, mr_no,
 null, amount,deposit_date, counter,bank_name, reference_no, username, mod_time,username, mod_time, remarks, 0, paid_by, 
 payment_mode_id, card_type_id, bank_batch_no,card_auth_code, card_holder_name , currency_id, exchange_rate, exchange_date, 
 currency_amt, card_expdate, card_number, credit_card_commission_percentage, credit_card_commission_amount,totp ,mob_number, 
 null, 0, deposit_date, null, false,amount,realized,true,deposit_payer_name,payer_phone_no,payer_address,0 FROM patient_deposits;

-- creating index on deposit_type column for quicker migration
CREATE INDEX patient_deposits_deposit_type_index ON patient_deposits(deposit_type);

-- inserting new values for writeoff
INSERT INTO unique_number VALUES ( 'write_off_patient', 1, 'PW', 'PW', '0000');
INSERT INTO unique_number VALUES ( 'write_off_sponsor', 1, 'SW', 'SW', '0000');

CREATE SEQUENCE patient_writeoff_receipt_sequence START 1;
COMMENT ON SEQUENCE patient_writeoff_receipt_sequence IS '{ "type": "Txn", "comment": "Holds patient write off receipt sequence" }';

CREATE SEQUENCE sponsor_writeoff_receipt_sequence START 1;
COMMENT ON SEQUENCE sponsor_writeoff_receipt_sequence IS '{ "type": "Txn", "comment": "Holds sponsor write off receipt sequence" }';

CREATE SEQUENCE writeoff_refund_receipt_sequence START 1;
COMMENT ON SEQUENCE writeoff_refund_receipt_sequence IS '{ "type": "Txn", "comment": "Holds write off refund receipt sequence" }';

INSERT INTO hosp_id_patterns(
pattern_id, std_prefix, date_prefix_pattern, num_pattern, sequence_name, sequence_reset_freq, date_prefix, type)
VALUES ('WRITEOFF_REFUND', 'WF', '', '000000', 'writeoff_refund_receipt_sequence', '', '', 'Txn');
