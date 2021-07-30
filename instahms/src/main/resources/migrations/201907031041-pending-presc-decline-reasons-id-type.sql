-- liquibase formatted sql
-- changeset vinaykumarjavalkar:pending-presc-decline-reasons-id-type

ALTER TABLE patient_pending_prescriptions alter column declined_reason_id TYPE INTEGER;