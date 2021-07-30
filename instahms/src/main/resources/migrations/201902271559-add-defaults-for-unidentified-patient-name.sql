-- liquibase formatted sql
-- changeset adityabhatia02:default-values-unidentified-patient-name

UPDATE registration_preferences SET unidentified_patient_first_name = 'JOHN';
UPDATE registration_preferences SET unidentified_patient_last_name = 'DOE';
ALTER TABLE registration_preferences ALTER COLUMN unidentified_patient_first_name SET DEFAULT 'JOHN';
ALTER TABLE registration_preferences ALTER COLUMN unidentified_patient_last_name SET DEFAULT 'DOE';