-- liquibase formatted sql
-- changeset javalkarvinay:added_created_at_column_in_allergies_table

ALTER TABLE patient_allergies ADD COLUMN created_at timestamp without time zone DEFAULT now();
UPDATE patient_allergies SET created_at=mod_time;
INSERT INTO code_system_categories VALUES (9,'diagnosis_statuses');
