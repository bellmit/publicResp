-- liquibase formatted sql
-- changeset tejakilaru:creating-code_systems

CREATE SEQUENCE code_systems_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE code_systems (
	id INTEGER PRIMARY KEY DEFAULT nextval('code_systems_seq'::regclass),
	label CHARACTER VARYING
);

COMMENT ON table code_systems is '{ "type": "Master", "comment": "Code Systems" }';
COMMENT ON sequence code_systems_seq is '{ "type": "Master", "comment": "" }';
