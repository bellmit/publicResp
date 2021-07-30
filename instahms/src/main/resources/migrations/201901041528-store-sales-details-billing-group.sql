-- liquibase formatted sql
-- changeset tejasiitb:store-sales-details-billing-group

ALTER TABLE store_sales_details ADD COLUMN billing_group_id integer;