-- liquibase formatted sql
-- changeset allabakash:pending-presc-decline-reasons

ALTER TABLE pending_prescription_declined_reasons ALTER COLUMN declined_reason_id TYPE INTEGER;