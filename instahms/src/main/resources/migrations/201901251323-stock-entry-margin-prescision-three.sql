-- liquibase formatted sql
-- changeset adeshatole:add-columns-for-margin-percent-and-amount-precision-three context:precision-3

ALTER TABLE store_supplier_contracts_item_rates ALTER COLUMN margin type numeric(16,3);