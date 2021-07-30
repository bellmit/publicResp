-- liquibase formatted sql
-- changeset goutham005:delete-action-right-for-vitals

INSERT INTO action_rights(SELECT role_id,'delete_vitals','A' FROM u_role);
