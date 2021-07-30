-- liquibase formatted sql
-- changeset manika.singh:add-column-for-exclusion-override

ALTER TABLE bill_charge ADD COLUMN item_excluded_from_doctor boolean;
ALTER TABLE bill_charge ADD COLUMN item_excluded_from_doctor_remarks character varying(500);

ALTER TABLE store_sales_details ADD COLUMN item_excluded_from_doctor boolean;
ALTER TABLE store_sales_details ADD COLUMN item_excluded_from_doctor_remarks character varying(500);
