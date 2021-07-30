-- liquibase formatted sql
-- changeset ranjansingh:<added-document-form-list-section-in-master>

INSERT INTO system_generated_sections (section_id, section_name, ip, service, op_follow_up_consult_form, display_name) VALUES ('-20', 'Documents & Forms (Sys)', 'Y', 'N', 'N', 'Documents & Forms');

INSERT INTO insta_section_rights (role_id,section_id) (SELECT ur.role_id,-20 FROM u_role ur WHERE ur.role_id != 1 AND ur.role_id != 2); 

