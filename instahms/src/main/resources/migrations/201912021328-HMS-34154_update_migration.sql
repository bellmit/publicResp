-- liquibase formatted sql
-- changeset manjular:update-preauth_activity_status-from-preauth_prescription_activities

UPDATE patient_pending_prescriptions ppp SET preauth_activity_status=ppa.preauth_act_status FROM preauth_prescription_activities ppa
WHERE ppp.preauth_activity_id=ppa.preauth_act_id;

