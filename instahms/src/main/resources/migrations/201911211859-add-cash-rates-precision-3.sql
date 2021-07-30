-- liquibase formatted sql
-- changeset adeshatole:add-cash-rates-prescision-3 context:precision-3

ALTER TABLE bill_charge_transaction ALTER COLUMN cash_rate TYPE NUMERIC(16,3);
