-- liquibase formatted sql
-- changeset suryakant.t:patient_registration_add_column_created_by

ALTER TABLE patient_registration ADD COLUMN created_by varchar(30);
