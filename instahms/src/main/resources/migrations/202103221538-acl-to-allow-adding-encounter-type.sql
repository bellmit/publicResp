-- liquibase formatted sql
-- changeset raeshmika:<acl-to-allow-adding-encounter-type-at-registration>


INSERT INTO action_rights (select role_id, 'allow_assigning_encounter_type', 'A' from u_role);

UPDATE action_rights set rights = 'A' where action='allow_assigning_encounter_type' and role_id in (1,2);