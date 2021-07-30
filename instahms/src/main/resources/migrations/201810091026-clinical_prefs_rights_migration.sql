-- liquibase formatted sql
-- changeset tejakilaru:clinical-preferences-screen-role-rights-migration

INSERT INTO screen_rights (SELECT role_id, 'mas_clinical_preferences' as screen_id, rights FROM screen_rights where screen_id = 'mas_gen_pref');
INSERT INTO url_action_rights (SELECT role_id, 'mas_clinical_preferences' as action_id, rights FROM url_action_rights where action_id='mas_gen_pref');

INSERT INTO screen_rights (SELECT role_id, 'clinical_prefs_audit_log' as screen_id, rights FROM screen_rights where screen_id = 'mas_gen_pref');
INSERT INTO url_action_rights (SELECT role_id, 'clinical_prefs_audit_log' as action_id, rights FROM url_action_rights where action_id='mas_gen_pref');
