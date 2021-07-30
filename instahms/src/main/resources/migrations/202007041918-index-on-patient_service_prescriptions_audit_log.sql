-- liquibase formatted sql
-- changeset dattuvs:index-on-field-name failOnError:false

CREATE INDEX patient_service_prescriptions_audit_log_field_name_idx ON patient_service_prescriptions_audit_log USING btree(field_name);