-- liquibase formatted sql
-- changeset dattuvs:index-on-vital_reading_audit_log failOnError:false

-- IES-966 --
CREATE INDEX vital_reading_audit_log_field_name_idx ON vital_reading_audit_log USING btree(field_name);