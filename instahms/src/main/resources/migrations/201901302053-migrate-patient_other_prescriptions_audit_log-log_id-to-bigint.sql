-- liquibase formatted sql
-- changeset prashantbaisla:migrate-patient_other_prescriptions_audit_log-log_id-to-bigint
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:integer SELECT data_type FROM information_schema.columns WHERE column_name = 'log_id' and table_name = 'patient_other_prescriptions_audit_log' and table_schema = current_schema();

ALTER TABLE patient_other_prescriptions_audit_log RENAME TO patient_other_prescriptions_audit_log_old;
CREATE TABLE patient_other_prescriptions_audit_log (LIKE patient_other_prescriptions_audit_log_old INCLUDING ALL);
ALTER TABLE patient_other_prescriptions_audit_log ALTER COLUMN log_id TYPE bigint;
