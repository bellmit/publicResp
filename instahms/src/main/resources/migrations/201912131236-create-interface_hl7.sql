-- liquibase formatted sql
-- changeset tejakilaru:creating-interface_hl7

CREATE SEQUENCE interface_hl7_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE interface_hl7 (
	id INTEGER PRIMARY KEY DEFAULT nextval('interface_hl7_seq'::regclass),
	ip_address CHARACTER VARYING NOT NULL,
	port INTEGER NOT NULL,
	code_systems_id INTEGER
);

COMMENT ON table interface_hl7 is '{ "type": "Master", "comment": "HL7 Interfaces" }';
COMMENT ON sequence interface_hl7_seq is '{ "type": "Master", "comment": "" }';
