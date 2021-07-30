-- liquibase formatted sql
-- changeset raeshmika:New-action_rights-for-edit-appointment

INSERT INTO action_rights (select role_id, 'edit_appointment_plan', 'N' from u_role);