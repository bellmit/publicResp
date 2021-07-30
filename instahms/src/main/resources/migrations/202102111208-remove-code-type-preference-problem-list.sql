-- liquibase formatted sql
-- changeset dattuvs:remove-codeType-preference-for-patient-problems failOnError:false

ALTER TABLE clinical_preferences RENAME COLUMN patient_problem_code_types TO obsolete_patient_problem_code_types;
