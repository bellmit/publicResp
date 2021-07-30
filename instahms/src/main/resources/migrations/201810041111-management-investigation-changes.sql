-- liquibase formatted sql
-- changeset tejakilaru:management_investigation_validity_changes

ALTER TABLE patient_test_prescriptions ADD COLUMN reorder CHARACTER(1) DEFAULT 'N';
