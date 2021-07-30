-- liquibase formatted sql
-- changeset tejakilaru:ipemr_revision_number

ALTER TABLE patient_registration ADD COLUMN ipemr_revision_number numeric; 
