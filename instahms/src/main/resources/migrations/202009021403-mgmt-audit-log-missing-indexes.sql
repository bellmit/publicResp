-- liquibase formatted sql
-- changeset pranays:management-audit-log-missing-indexes failOnError:false

CREATE INDEX pspal_operation_field_name_idx ON patient_service_prescriptions_audit_log USING btree (operation,field_name);

CREATE INDEX pcpal_operation_field_name_idx ON patient_consultation_prescriptions_audit_log USING btree (operation,field_name);

CREATE INDEX poppal_operation_field_name_idx ON patient_operation_prescriptions_audit_log USING btree (operation,field_name);

CREATE INDEX popal_operation_field_name_idx ON patient_other_prescriptions_audit_log USING btree (operation,field_name);

CREATE INDEX pompal_operation_field_name_idx ON patient_other_medicine_prescriptions_audit_log USING btree (operation,field_name);

CREATE INDEX ptpal_field_name_idx ON patient_test_prescriptions_audit_log USING btree (field_name);

CREATE INDEX pmpal_field_name_idx ON patient_medicine_prescriptions_audit_log USING btree (field_name);