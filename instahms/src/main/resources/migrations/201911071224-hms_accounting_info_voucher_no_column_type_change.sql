-- liquibase formatted sql
-- changeset abhishekv31:changing-column-type-varchar-from-char-100
ALTER TABLE hms_accounting_info ALTER COLUMN voucher_no TYPE varchar;
