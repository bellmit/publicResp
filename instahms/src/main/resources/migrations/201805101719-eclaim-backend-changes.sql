-- liquibase formatted sql
-- changeset deepakpracto:eclaim-backend-changes.sql splitStatements:false

-- === update column data type size to hold the maximum character ==== 

ALTER table insurance_submission_batch ALTER COLUMN processing_error TYPE character varying(30000);

