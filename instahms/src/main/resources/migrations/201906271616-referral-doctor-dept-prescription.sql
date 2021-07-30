-- liquibase formatted sql
-- changeset sonam009:prescription-for-referral-doctor-and-department

ALTER TABLE patient_consultation_prescriptions ADD COLUMN dept_id CHARACTER VARYING(50);
ALTER TABLE patient_consultation_prescriptions ADD presc_activity_type CHARACTER VARYING DEFAULT 'DOC';

