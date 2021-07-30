-- liquibase formatted sql
-- changeset raeshmika:<acl-for-allow-overbook-appt>

INSERT INTO action_rights (select role_id, 'allow_appt_overbooking', 'Y' from u_role);