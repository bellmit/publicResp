-- liquibase formatted sql
-- changeset adeshatole:remove-not-null-constraint-for-supplier-rate

ALTER TABLE store_supplier_contracts_item_rates ALTER COLUMN supplier_rate DROP NOT NULL;