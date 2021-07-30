-- liquibase formatted sql
-- changeset tejakilaru:added-remarks-in-patient_iv_administered_details

ALTER TABLE patient_iv_administered_details ADD COLUMN remarks character varying(5000);
