-- liquibase formatted sql
-- changeset manjular:removing-the-characted-limit-to-center-id-column

ALTER TABLE discount_authorizer ALTER COLUMN center_id TYPE character varying;

