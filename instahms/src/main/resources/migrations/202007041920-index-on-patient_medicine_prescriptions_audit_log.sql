-- liquibase formatted sql
-- changeset dattuvs:indexing-on-field-name failOnError:false

CREATE INDEX patient_medicine_prescriptions_audit_log_field_name_idx ON patient_medicine_prescriptions_audit_log USING btree(field_name);