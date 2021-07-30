-- liquibase formatted sql
-- changeset anupamamr:stock-take-inactive-item-exclusion

ALTER TABLE physical_stock_take ADD COLUMN inactive_item_excl_dt date;
ALTER TABLE stores ADD COLUMN stock_take_item_exp_months integer;
