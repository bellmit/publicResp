-- liquibase formatted sql
-- changeset tejakilaru:creating-code_system_categories

CREATE TABLE code_system_categories (
	id INTEGER PRIMARY KEY,
	label CHARACTER VARYING
);

COMMENT ON table code_system_categories is '{ "type": "Master", "comment": "System Code Categories" }';
