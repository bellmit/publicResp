-- liquibase formatted sql
-- changeset manasaparam:adding-patient-name-in-hmsaccountinginfo

ALTER TABLE hms_accounting_info ADD COLUMN patient_name character varying(100);