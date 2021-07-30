-- liquibase formatted sql
-- changeset tejakilaru:creating-events_hl7

CREATE TABLE events_hl7 (
	id INTEGER PRIMARY KEY,
	event CHARACTER VARYING NOT NULL 
);

COMMENT ON table events_hl7 is '{ "type": "Master", "comment": "HL7 Events" }';
