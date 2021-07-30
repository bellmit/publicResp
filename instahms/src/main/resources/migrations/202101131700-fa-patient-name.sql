-- liquibase formatted sql
-- changeset manasaparam:increased-patient-name

Alter table hms_accounting_info alter column patient_name type character varying(400);