-- liquibase formatted sql
-- changeset janakivg:few-ip-op-prescription-preferences

ALTER TABLE clinical_preferences ADD COLUMN ip_prescription_discontinue_durations CHARACTER(1) NOT NULL DEFAULT 'Y';
ALTER TABLE clinical_preferences ADD COLUMN op_prescription_urgent_priority CHARACTER(1) NOT NULL DEFAULT 'Y';
ALTER TABLE clinical_preferences ADD COLUMN ip_prescription_urgent_priority CHARACTER(1) NOT NULL DEFAULT 'Y';

