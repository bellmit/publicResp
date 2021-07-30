-- liquibase formatted sql
-- changeset javalkarvinay:clinical_preference_for_patient_problem_code_type

ALTER TABLE clinical_preferences ADD COLUMN patient_problem_code_types VARCHAR DEFAULT 'ICD' NOT NULL;
