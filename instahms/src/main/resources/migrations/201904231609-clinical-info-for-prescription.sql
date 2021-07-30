-- liquibase formatted sql
-- changeset janakivg:clinical-notes-for-test-conduction-in-prescription

ALTER TABLE diagnostics ADD COLUMN mandate_clinical_info character(1) DEFAULT 'N'::bpchar NOT NULL, ADD COLUMN clinical_justification character varying;

ALTER TABLE patient_test_prescriptions ADD COLUMN clinical_note_for_conduction character varying, ADD COLUMN clinical_justification_for_item character varying;
