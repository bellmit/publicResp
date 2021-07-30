-- liquibase formatted sql
-- changeset pranays:<edit-action-rights-for-vitals>

INSERT INTO action_rights(SELECT role_id, 'edit_vitals', 'A' FROM u_role);