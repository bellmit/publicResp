-- liquibase formatted sql
-- changeset allabakash:Bill-receipt-refund-reference

ALTER TABLE bill_charge_receipt_allocation ADD COLUMN refund_reference_id BIGINT DEFAULT NULL;
ALTER TABLE bill_charge_receipt_allocation ADD COLUMN activity VARCHAR(15) DEFAULT NULL;
