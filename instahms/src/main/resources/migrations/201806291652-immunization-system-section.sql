-- liquibase formatted sql
-- changeset tejakilaru:<immunization-system-section>

ALTER TABLE system_generated_sections DROP COLUMN obsolete_visit_type;
ALTER TABLE system_generated_sections DROP COLUMN obsolete_form_type;

INSERT INTO system_generated_sections (section_id, section_name, service, triage, op_follow_up_consult_form, display_name)
	VALUES ('-17', 'Immunization Up-To-Date (Sys)', 'N', 'Y', 'N', 'Immunization Up-To-Date');

ALTER TABLE doctor_consultation ADD COLUMN immunization_remarks CHARACTER VARYING;

INSERT INTO insta_section_rights (role_id,section_id)
	(SELECT ur.role_id,-17 FROM u_role ur WHERE ur.role_id != 1 AND ur.role_id != 2);
