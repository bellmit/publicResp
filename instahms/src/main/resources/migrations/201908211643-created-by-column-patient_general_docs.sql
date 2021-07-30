-- liquibase formatted sql
-- changeset pranays:<add-created-by-in-patient-general-documents>

ALTER TABLE patient_general_docs ADD COLUMN created_by varchar(50);
