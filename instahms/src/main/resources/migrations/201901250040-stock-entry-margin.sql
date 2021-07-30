-- liquibase formatted sql
-- changeset adeshatole:add-columns-for-margin-percent-and-amount

ALTER TABLE store_supplier_contracts_item_rates ADD COLUMN margin NUMERIC(15,2), ADD COLUMN margin_type CHARACTER(1);
