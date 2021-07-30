-- liquibase formatted sql
-- changeset rajendratalekar:add-receipt-column-failed-accounting-export

ALTER TABLE accounting_failed_exports ADD COLUMN receipt_id VARCHAR(15);
ALTER TABLE accounting_failed_exports ALTER COLUMN bill_no DROP NOT NULL;
ALTER TABLE accounting_failed_exports ALTER COLUMN visit_id DROP NOT NULL;