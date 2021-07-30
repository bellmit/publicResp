-- liquibase formatted sql
-- changeset allabakash:pending prescriptions preauth column

ALTER TABLE patient_pending_prescriptions ADD COLUMN preauth_activity_id BIGINT;
