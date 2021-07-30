-- liquibase formatted sql
-- changeset manjular:adding-new-column-enable-patient-deposit-availability-in-generic-pref

ALTER TABLE generic_preferences ADD COLUMN enable_patient_deposit_availability CHARACTER VARYING(1) DEFAULT 'A'::bpchar;
COMMENT ON COLUMN generic_preferences.enable_patient_deposit_availability IS 'Values : (A,E) , A - For all Centers, E - For Each Center';
