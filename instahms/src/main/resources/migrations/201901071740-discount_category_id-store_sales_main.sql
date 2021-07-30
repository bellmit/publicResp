-- liquibase formatted sql
-- changeset tejasiitb:add-discount_category_id-store_sales_main

ALTER TABLE store_sales_main ADD COLUMN discount_category_id integer;