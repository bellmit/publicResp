-- liquibase formatted sql
-- changeset allabakash:pending_prescription_update_preauth_activity_status
UPDATE patient_pending_prescriptions ppp SET preauth_activity_status = preauth_act_status 
    FROM preauth_prescription_activities ppa WHERE ppa.preauth_act_id = ppp.preauth_activity_id 
    AND ppp.preauth_activity_status IS NULL;