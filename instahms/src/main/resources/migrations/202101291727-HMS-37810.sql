-- liquibase formatted sql
-- changeset manjular:override_online_prior_auth_status failOnError:false

INSERT INTO action_rights (select role_id, 'override_online_prior_auth_status', 'N' from u_role);
