-- liquibase formatted sql
-- changeset janakivg:note-type-and-notes-auditlog-screen-rights

INSERT INTO screen_rights (SELECT distinct sr.role_id, 'mas_note_types' as screen_id, 'A' as rights 
FROM u_user u JOIN user_hosp_role_master uhr ON (u.emp_username = uhr.u_user) 
JOIN screen_rights sr ON (sr.role_id=u.role_id) WHERE uhr.hosp_role_id <0 AND rights='A');

INSERT INTO url_action_rights (SELECT distinct uar.role_id, 'mas_note_types' as action_id, 'A' as rights 
FROM u_user u JOIN user_hosp_role_master uhr ON (u.emp_username = uhr.u_user) 
JOIN url_action_rights uar ON (uar.role_id=u.role_id) WHERE uhr.hosp_role_id <0 AND rights='A');

INSERT INTO screen_rights (SELECT distinct role_id, 'patient_notes_audit_log' as screen_id, 'A' as rights FROM screen_rights where (screen_id = 'doctor_notes_audit_log' or screen_id='nurse_notes_audit_log') and rights='A');
INSERT INTO url_action_rights (SELECT distinct role_id, 'patient_notes_audit_log' as action_id, 'A' as rights FROM url_action_rights where (action_id = 'doctor_notes_audit_log' or action_id='nurse_notes_audit_log') and rights='A');
