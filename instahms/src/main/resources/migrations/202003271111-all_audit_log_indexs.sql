-- liquibase formatted sql
-- changeset anandpatel:index-for-all-audit-log-tables

CREATE INDEX ptpal_log_id_idx ON patient_test_prescriptions_audit_log USING btree (log_id);

CREATE INDEX ppal_log_id_idx ON patient_prescription_audit_log USING btree (log_id);

CREATE INDEX pspal_log_id_idx ON patient_service_prescriptions_audit_log USING btree (log_id);

CREATE INDEX pcpal_log_id_idx ON patient_consultation_prescriptions_audit_log USING btree (log_id);

CREATE INDEX poppal_log_id_idx ON patient_operation_prescriptions_audit_log USING btree (log_id);

CREATE INDEX popal_log_id_idx ON patient_other_prescriptions_audit_log USING btree (log_id);

CREATE INDEX pompal_log_id_idx ON patient_other_medicine_prescriptions_audit_log USING btree (log_id);

CREATE INDEX pmpal_log_id_idx ON patient_medicine_prescriptions_audit_log USING btree (log_id);

CREATE INDEX ptpal_op_test_pres_id_idx ON patient_test_prescriptions_audit_log USING btree (op_test_pres_id);

CREATE INDEX ppal_patient_presc_id_idx ON patient_prescription_audit_log USING btree (patient_presc_id);

CREATE INDEX pspal_op_service_pres_id_idx ON patient_service_prescriptions_audit_log USING btree (op_service_pres_id);

CREATE INDEX pcpal_prescription_id_idx ON patient_consultation_prescriptions_audit_log USING btree (prescription_id);

CREATE INDEX poppal_prescription_id_idx ON patient_operation_prescriptions_audit_log USING btree (prescription_id);

CREATE INDEX popal_prescription_id_idx ON patient_other_prescriptions_audit_log USING btree (prescription_id);

CREATE INDEX pomal_prescription_id_idx ON patient_other_medicine_prescriptions_audit_log USING btree (prescription_id);

CREATE INDEX pmpal_op_medicine_pres_id_idx ON patient_medicine_prescriptions_audit_log USING btree (op_medicine_pres_id);
