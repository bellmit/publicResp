-- liquibase formatted sql
-- changeset sonam009:ip-emr-default-screen-right-and-action-right

INSERT INTO screen_rights (SELECT distinct (role_id), 'new_ipemr', 'A' FROM screen_rights 
WHERE screen_id IN('visit_summary','ip_prescriptions','doctors_note','nurse_note','patient_summary') AND rights ='A');

INSERT INTO action_rights (SELECT sr.role_id, 'allow_customize_sections', 'A' FROM screen_rights sr
WHERE not exists (SELECT * FROM action_rights ar WHERE ar.action='allow_customize_sections' AND ar.role_id = sr.role_id) AND screen_id='visit_summary' AND rights ='A');
