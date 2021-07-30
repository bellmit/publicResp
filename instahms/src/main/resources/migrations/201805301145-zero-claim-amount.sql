-- liquibase formatted sql
-- changeset deepakpracto:zero-claim-amount

-- === update new column for already existing row for e-claim generation changes ==== 

ALTER TABLE services ADD COLUMN allow_zero_claim_amount character varying(1) NOT NULL DEFAULT 'n';
ALTER TABLE diagnostics ADD COLUMN allow_zero_claim_amount character varying(1) NOT NULL DEFAULT 'n';
ALTER TABLE operation_master ADD COLUMN allow_zero_claim_amount character varying(1) NOT NULL DEFAULT 'n';
ALTER TABLE bed_types ADD COLUMN allow_zero_claim_amount character varying(1) NOT NULL DEFAULT 'n';
ALTER TABLE theatre_master ADD COLUMN allow_zero_claim_amount character varying(1) NOT NULL DEFAULT 'n';
ALTER TABLE anesthesia_type_master ADD COLUMN allow_zero_claim_amount character varying(1) NOT NULL DEFAULT 'n';
ALTER TABLE consultation_types ADD COLUMN allow_zero_claim_amount character varying(1) NOT NULL DEFAULT 'n';
ALTER TABLE store_item_details ADD COLUMN allow_zero_claim_amount character varying(1) NOT NULL DEFAULT 'n';
