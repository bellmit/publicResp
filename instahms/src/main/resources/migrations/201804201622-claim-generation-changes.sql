-- liquibase formatted sql
-- changeset deepakpracto:claim-generation-changes.sql splitStatements:false

-- === added new column for e-claim geration changes ==== 
ALTER TABLE insurance_submission_batch ADD COLUMN processing_error character varying(3000);
ALTER TABLE insurance_submission_batch ADD COLUMN processing_status character varying(1) NOT NULL DEFAULT 'N';
ALTER TABLE insurance_submission_batch ADD COLUMN processing_type character varying(1);