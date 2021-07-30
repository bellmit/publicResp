-- liquibase formatted sql
-- changeset akshaysuman:doctor_login_controls

ALTER TABLE u_user ADD COLUMN login_controls_applicable character(1) DEFAULT 'Y'::bpchar;
UPDATE u_user SET login_controls_applicable = 'Y' ;
ALTER TABLE u_user ALTER COLUMN login_controls_applicable SET NOT NULL;
