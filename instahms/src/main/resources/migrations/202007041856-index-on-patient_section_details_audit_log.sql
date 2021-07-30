-- liquibase formatted sql
-- changeset dattuvs:indexing-on-patient_section_details_audit_log  failOnError:false

-- IES-969 ----
CREATE INDEX patient_section_details_audit_log_field_name_idx ON patient_section_details_audit_log USING btree(field_name);