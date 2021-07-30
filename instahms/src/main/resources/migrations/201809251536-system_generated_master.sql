-- liquibase formatted sql
-- changeset sonam009:added-ip-precsription-and-notes-in-master

UPDATE system_generated_sections SET ip='Y' WHERE section_id='-7' ;

INSERT INTO system_generated_sections (section_id, section_name, ip, service, op_follow_up_consult_form, display_name) VALUES ('-18', 'Notes (Sys)', 'Y', 'N', 'N', 'Notes');

INSERT INTO insta_section_rights (role_id,section_id) (SELECT ur.role_id,-18 FROM u_role ur WHERE ur.role_id != 1 AND ur.role_id != 2);