-- liquibase formatted sql
-- changeset sirisharl:<pending-activity_type_prerence>
ALTER TABLE generic_preferences ADD COLUMN bill_pending_validation_activity_types CHARACTER VARYING(15) DEFAULT 'L,R,C';
COMMENT on COLUMN generic_preferences.bill_pending_validation_activity_types is
' L - Laboratory, R - Radiology, C - Consultation ';
