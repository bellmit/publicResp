-- liquibase formatted sql
-- changeset kchetan:alter-bill-receipts-table-remarks-length

ALTER TABLE bill_receipts ALTER COLUMN remarks TYPE VARCHAR(100);

ALTER TABLE patient_deposits ALTER COLUMN remarks TYPE VARCHAR(100);
