-- liquibase formatted sql
-- changeset TejaKilaru:template-changes

ALTER TABLE form_components ADD COLUMN istemplate BOOLEAN DEFAULT false;

CREATE TABLE form_vital_parameters(
	form_id      integer NOT NULL,
	vital_param_id       integer NOT NULL
);