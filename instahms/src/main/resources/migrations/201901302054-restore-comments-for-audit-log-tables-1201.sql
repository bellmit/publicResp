-- liquibase formatted sql
-- changeset rajendratalekar:restore-comments-for-audit-log-tables-1201

COMMENT ON table doctor_consultation_audit_log is '{ "type": "Txn", "comment": "Doctor Consultation Audit Log" } ';
COMMENT ON table clinical_preferences_audit_log is '{ "type": "Master", "comment": "Clinical Preferences Audit Log" } ';
