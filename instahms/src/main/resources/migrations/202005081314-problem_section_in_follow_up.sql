-- liquibase formatted sql
-- changeset javalkarvinay:problem_section_in_follow_up

UPDATE system_generated_sections SET op_follow_up_consult_form = 'Y' WHERE section_id='-21';
