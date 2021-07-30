-- liquibase formatted sql
-- changeset adeshatole:add-cash-rate-column

ALTER TABLE bill_charge_transaction ADD COLUMN cash_rate NUMERIC(15,2);