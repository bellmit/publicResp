-- liquibase formatted sql
-- changeset dattuvs:indexing-on-fieldname failOnError:false

-- IES-968 ---
CREATE INDEX patient_test_prescriptions_audit_log_field_name_idx ON patient_test_prescriptions_audit_log USING btree(field_name);