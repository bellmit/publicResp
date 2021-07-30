-- liquibase formatted sql
-- changeset rajendratalekar:add-bill-last-finalized-hms-accounting

ALTER TABLE hms_accounting_info ADD COLUMN bill_last_finalized_date timestamp without time zone;

CREATE INDEX hai_bill_last_finalized_date_idx on hms_accounting_info(bill_last_finalized_date);