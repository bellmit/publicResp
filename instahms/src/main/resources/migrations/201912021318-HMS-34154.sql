-- liquibase formatted sql
-- changeset manjular:adding-new-columns-preauth_activity_status-approved-aty-remaining-apporved-qty

ALTER TABLE patient_pending_prescriptions ADD COLUMN preauth_activity_status CHARACTER VARYING(1);
COMMENT ON COLUMN patient_pending_prescriptions.preauth_activity_status IS 'Valid values are: O/S/D/C: Open/Sent/Denied/Approved';

ALTER TABLE preauth_prescription_activities ADD COLUMN approved_qty INTEGER NOT NULL DEFAULT 0;
ALTER TABLE preauth_prescription_activities ADD COLUMN rem_approved_qty INTEGER NOT NULL DEFAULT 0;
