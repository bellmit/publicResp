-- liquibase formatted sql
-- changeset junzy:migrations-for-erx-support-for-non-pbm-emirates-clinic

ALTER TABLE insurance_claim ADD COLUMN is_external_pbm BOOLEAN DEFAULT FALSE;
ALTER TABLE store_sales_details ADD COLUMN erx_activity_id VARCHAR(50);
ALTER TABLE store_sales_main ADD COLUMN erx_reference_no VARCHAR(50);