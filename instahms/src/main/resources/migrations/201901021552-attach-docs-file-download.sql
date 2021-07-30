-- liquibase formatted sql
-- changeset junaidahmed:adds-support-for-patient-document-download

ALTER TABLE patient_documents ADD COLUMN filename VARCHAR(1024);