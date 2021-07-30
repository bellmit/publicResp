-- liquibase formatted sql
-- changeset tejakilaru:creating-interface_details_hl7

CREATE SEQUENCE interface_details_hl7_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE interface_details_hl7 (
	id INTEGER PRIMARY KEY DEFAULT nextval('interface_details_hl7_seq'::regclass),
	sending_facility CHARACTER VARYING NOT NULL,
	sending_application CHARACTER VARYING NOT NULL,
	receving_facility CHARACTER VARYING NOT NULL,
	receving_application CHARACTER VARYING NOT NULL
);

COMMENT ON table interface_details_hl7 is '{ "type": "Master", "comment": "HL7 Interfaces Details" }';
COMMENT ON sequence interface_details_hl7_seq is '{ "type": "Master", "comment": "" }';
