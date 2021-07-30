-- liquibase formatted sql
-- changeset rajendratalekar:drop-unused-column-test-detials-audit-log
ALTER TABLE patient_medicine_prescriptions_audit_log DROP COLUMN medicine_name;