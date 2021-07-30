-- liquibase formatted sql
-- changeset vinaykumarjavalkar:pending-prescription-add-column

ALTER TABLE patient_pending_prescriptions ADD COLUMN followup_id CHARACTER VARYING(19);
ALTER TABLE patient_pending_prescriptions ALTER COLUMN patient_presc_id TYPE BIGINT USING(patient_presc_id::INTEGER);
ALTER TABLE patient_pending_prescriptions ALTER COLUMN patient_presc_id DROP NOT NULL;

CREATE INDEX idx_patient_pending_prescriptions_patient_presc_id ON patient_pending_prescriptions(patient_presc_id);