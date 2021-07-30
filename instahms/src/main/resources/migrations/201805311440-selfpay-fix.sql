-- liquibase formatted sql
-- changeset junzy:fixed-file-name-column-type

ALTER TABLE selfpay_submission_batch ALTER COLUMN file_name TYPE VARCHAR;

