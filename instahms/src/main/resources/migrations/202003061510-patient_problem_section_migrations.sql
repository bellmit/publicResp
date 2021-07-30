-- liquibase formatted sql
-- changeset javalkarvinay:patient_problem_section_migrations

ALTER TABLE insta_section_rights ADD CONSTRAINT section_role_unique UNIQUE(section_id,role_id);
INSERT INTO insta_section_rights (section_id,role_id) (SELECT DISTINCT -21 AS section_id,uu.role_id FROM hospital_roles_master hrs 
       LEFT JOIN user_hosp_role_master uhrm ON hrs.hosp_role_id = uhrm.hosp_role_id
       LEFT JOIN u_user uu ON uu.emp_username = uhrm.u_user
       WHERE hrs.hosp_role_name='Doctor');
