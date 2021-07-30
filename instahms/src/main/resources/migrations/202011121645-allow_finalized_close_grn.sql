-- liquibase formatted sql
-- changeset manjular:allow_finalize_close_grn failOnError:false

INSERT INTO action_rights (select role_id, 'allow_finalize_close_grn', 'A' from u_role);

