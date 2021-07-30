-- liquibase formatted sql
-- changeset tejasiitb:patient_package_contents->visit_id
ALTER TABLE patient_package_contents ADD COLUMN visit_id character varying(20);