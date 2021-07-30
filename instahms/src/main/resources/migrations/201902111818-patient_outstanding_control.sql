-- liquibase formatted sql
-- changeset akshaysuman:patient_outstanding_control

ALTER TABLE registration_preferences ADD COLUMN patient_outstanding_control varchar(10) default 'Warn' NOT NULL;