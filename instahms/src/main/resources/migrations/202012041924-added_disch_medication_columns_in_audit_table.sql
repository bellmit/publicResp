-- liquibase formatted sql
-- changeset javalkarvinay:added_disch_medication_columns_in_audit_table

ALTER TABLE patient_medicine_prescriptions_audit ADD COLUMN stopped_user CHARACTER VARYING(30);
ALTER TABLE patient_medicine_prescriptions_audit ADD COLUMN stopped_reason CHARACTER VARYING(2000);
ALTER TABLE patient_medicine_prescriptions_audit ADD COLUMN stopped_date TIMESTAMP;
ALTER TABLE patient_medicine_prescriptions_audit ADD COLUMN stopped_doctor_id CHARACTER VARYING(15);
ALTER TABLE patient_medicine_prescriptions_audit ADD COLUMN is_discharge_medication BOOLEAN;

ALTER TABLE patient_other_medicine_prescriptions_audit ADD COLUMN is_discharge_medication BOOLEAN;
ALTER TABLE patient_other_prescriptions_audit ADD COLUMN is_discharge_medication BOOLEAN;