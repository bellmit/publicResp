-- liquibase formatted sql
-- changeset sonam009:acl-for-customize-sections

INSERT INTO action_rights ( SELECT distinct (role_id), 'allow_customize_sections', 'A' FROM screen_rights
 WHERE screen_id IN('new_triage','new_cons','new_initial_assessment','new_ipemr') AND rights ='A');
