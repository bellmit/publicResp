-- liquibase formatted sql
-- changeset allabakash:pending-prescription-schedule-table

ALTER TABLE scheduler_appointments ADD COLUMN patient_presc_id BIGINT;
ALTER TABLE patient_pending_prescriptions ADD COLUMN dept_id CHARACTER VARYING(50);
ALTER TABLE patient_pending_prescriptions ADD COLUMN presc_item_dept_id CHARACTER VARYING (50);