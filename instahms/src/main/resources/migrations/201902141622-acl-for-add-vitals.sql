-- liquibase formatted sql
-- changeset pranays:<acl-for-add-vitals>

INSERT INTO action_rights(SELECT role_id, 'add_vitals', 'A' FROM u_role);