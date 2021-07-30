-- liquibase formatted sql
-- changeset tejakilaru:<prescription-insurance-exclusion-attributes>

ALTER TABLE patient_prescription ADD COLUMN item_excluded_from_doctor boolean;
ALTER TABLE patient_prescription ADD COLUMN item_excluded_from_doctor_remarks character varying(500);
