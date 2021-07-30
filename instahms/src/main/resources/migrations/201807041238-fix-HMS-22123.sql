-- liquibase formatted sql
-- changeset adeshatole:increase-field-length

ALTER TABLE referral ALTER COLUMN referal_mobileno TYPE character varying(16);
ALTER TABLE patient_deposits ALTER COLUMN payer_phone_no TYPE character varying(16);
ALTER TABLE hl7_order_items ALTER COLUMN referal_mobileno TYPE character varying(16);