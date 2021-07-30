-- liquibase formatted sql
-- changeset tejakilaru:index_on_insta_section_audit_log_tables failOnError:false

CREATE INDEX psfal_section_detail_id_idx on patient_section_fields_audit_log(section_detail_id);
CREATE INDEX psoal_field_detail_id_idx on patient_section_options_audit_log(field_detail_id);
