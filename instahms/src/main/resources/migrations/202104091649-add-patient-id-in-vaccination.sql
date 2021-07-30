-- liquibase formatted sql
-- changeset dattuvs:add-patient-id-in-vaccination failOnError:false
ALTER TABLE patient_vaccination ADD COLUMN patient_id VARCHAR(15) DEFAULT NULL;
