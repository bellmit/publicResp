-- liquibase formatted sql
-- changeset janakivg:patient-section-values-audit-log-index

create index psval_section_detail_id_idx_text on patient_section_values_audit_log (text(section_detail_id));