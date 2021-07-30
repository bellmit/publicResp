-- liquibase formatted sql
-- changeset adityabhatia02:create-er-registration-columns

ALTER TABLE patient_details ADD COLUMN is_unidentified_patient BOOLEAN DEFAULT false;
ALTER TABLE patient_registration ADD COLUMN is_ER_visit BOOLEAN DEFAULT false;

