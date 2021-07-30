-- liquibase formatted sql
-- changeset tejakilaru:consultation-audit-rights-migration

INSERT INTO screen_rights (SELECT distinct role_id, 'cons_summary_audit_log' as screen_id, 'A' as rights FROM screen_rights where (screen_id = 'cons_audit_log' or screen_id='pres_audit_log') and rights='A');
INSERT INTO url_action_rights (SELECT distinct role_id, 'cons_summary_audit_log' as action_id, 'A' as rights FROM url_action_rights where (action_id = 'cons_audit_log' or action_id='pres_audit_log') and rights='A');
