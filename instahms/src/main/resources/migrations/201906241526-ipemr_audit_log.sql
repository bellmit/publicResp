-- liquibase formatted sql
-- changeset sonam009:ipemr-action-audit-log

ALTER TABLE patient_registration ADD COLUMN ipemr_complete_time timestamp with time zone;
INSERT INTO screen_rights (SELECT distinct role_id, 'ipemr_audit_log' as screen_id, 'A' as rights FROM screen_rights where (screen_id = 'ip_audit_log') and rights='A');
INSERT INTO url_action_rights (SELECT distinct role_id, 'ipemr_audit_log' as action_id, 'A' as rights FROM url_action_rights where (action_id = 'ip_audit_log') and rights='A');