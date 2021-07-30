-- liquibase formatted sql
-- changeset tejakilaru:creating-message_mapping_hl7

CREATE TABLE message_mapping_hl7 (
	event_id INTEGER REFERENCES events_hl7(id),
    message_type_id CHARACTER VARYING(50) NOT NULL,
    priority INTEGER default 0 NOT NULL,
    retry_on_fail boolean default false NOT NULL,
    center_id INTEGER default 0,
    interface_id INTEGER REFERENCES interface_hl7(id),
    interface_details_id INTEGER REFERENCES interface_details_hl7(id)
    
);

COMMENT ON table message_mapping_hl7 is '{ "type": "Master", "comment": "Message Mappings HL7" }';
