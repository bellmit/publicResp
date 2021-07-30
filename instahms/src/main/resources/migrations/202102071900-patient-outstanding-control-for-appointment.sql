-- liquibase formatted sql
-- changeset asif:patient_outstanding_control_for_appointment

ALTER TABLE generic_preferences ADD COLUMN patient_outstanding_control_for_appointment varchar(10) default 'Warn' NOT NULL;
