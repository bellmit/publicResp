-- liquibase formatted sql
-- changeset adeshatole:fix-bill-charge-transaction

ALTER TABLE bill_charge_transaction	ADD CONSTRAINT unique_charge_id UNIQUE (bill_charge_id);
ALTER TABLE bill_charge_transaction	ALTER COLUMN bill_charge_id SET NOT NULL;