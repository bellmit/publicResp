-- liquibase formatted sql
-- changeset junaidahmed:adds-table-for-supporting-documents-for-diagnosis
-- validCheckSum: ANY

ALTER TABLE mrd_observations ADD COLUMN document_id INTEGER;

-- introducing created_at and mod_time columns for new patient_document entries only
ALTER TABLE patient_documents ADD COLUMN created_at TIMESTAMP, ADD COLUMN mod_time TIMESTAMP;


COMMENT ON COLUMN mrd_observations.document_id IS 'Stores attached documents uploaded against a charge';
INSERT INTO mrd_supported_codes VALUES ('Observations','File','',nextval('mrd_supported_codes_seq'));