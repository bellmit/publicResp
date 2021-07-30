-- liquibase formatted sql
-- changeset junaidahmed:fixes-for-erx-external-pbm-feature
ALTER TABLE store_sales_main ADD COLUMN is_external_pbm BOOLEAN DEFAULT FALSE;
ALTER TABLE insurance_claim DROP COLUMN is_external_pbm;
ALTER TABLE insurance_submission_batch ADD COLUMN is_external_pbm_batch BOOLEAN DEFAULT FALSE;
