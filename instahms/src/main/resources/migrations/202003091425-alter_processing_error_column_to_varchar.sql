-- liquibase formatted sql
-- changeset abhishekv31:change-the-column-type-processing-error-to-varchar
ALTER TABLE insurance_submission_batch ALTER COLUMN processing_error TYPE varchar;
