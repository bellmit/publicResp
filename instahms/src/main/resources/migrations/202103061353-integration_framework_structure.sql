-- liquibase formatted sql
-- changeset javalkarvinay:integration_framework_structure

-- Column to store event processing id.
ALTER TABLE export_message_queue ADD COLUMN event_processing_id BIGINT;

-- Table to add events.
CREATE SEQUENCE hie_events_seq START 1;
COMMENT ON SEQUENCE hie_events_seq IS '{ "type": "Master", "comment": "Holds sequence for hie_events_seq" }';

CREATE TABLE hie_events (
 event_id INT DEFAULT nextval('hie_events_seq'),
 event_name VARCHAR(100),
 status VARCHAR(1) DEFAULT 'A',
 event_description VARCHAR(350),
 PRIMARY KEY (event_id)
);
COMMENT ON table hie_events is '{ "type": "Master", "comment": "HIE events master" }';

-- Table to configure interfaces.
CREATE SEQUENCE interface_config_master_seq START 1;
COMMENT ON SEQUENCE interface_config_master_seq IS '{ "type": "Master", "comment": "Holds sequence for interface_config_master_seq" }';

CREATE TABLE interface_config_master (
 interface_id INT DEFAULT nextval('interface_config_master_seq'),
 interface_type VARCHAR(10) NOT NULL,
 interface_name VARCHAR(100) NOT NULL,
 connection_type VARCHAR(20) NOT NULL,
 code_system_id INT REFERENCES code_systems(id),
 status CHARACTER(1) DEFAULT 'A',
 connection_status VARCHAR(15) DEFAULT 'WAITING',
 destination_host VARCHAR,
 destination_port INT,
 timeout_in_sec INT DEFAULT 5,
 retry_max_count INT DEFAULT 50,
 retry_for_days INT DEFAULT 0,
 retry_interval_in_minutes INT DEFAULT 15,
 sending_facility VARCHAR(100),
 sending_application VARCHAR(100),
 receving_facility VARCHAR(100),
 receving_application VARCHAR(100),
 uri VARCHAR,
 req_parameters TEXT DEFAULT '{}',
 created_at TIMESTAMP DEFAULT now(),
 created_by VARCHAR(30) NOT NULL,
 modified_at TIMESTAMP DEFAULT now(),
 modified_by VARCHAR(30) NOT NULL,
 PRIMARY KEY (interface_id),
 CHECK(connection_type IN ('socket','https')),
 CHECK(connection_status IN ('WAITING','STARTED','STOPPED','RESCHEDULED'))
);
COMMENT ON table interface_config_master is '{ "type": "Master", "comment": "Interface configuration master" }';

-- Table to map events to interfaces.
CREATE SEQUENCE interface_event_mapping_seq START 1;
COMMENT ON SEQUENCE interface_event_mapping_seq IS '{ "type": "Master", "comment": "Holds sequence for interface_event_mapping_seq" }';

CREATE TABLE interface_event_mapping (
 event_mapping_id INT DEFAULT nextval('interface_event_mapping_seq'),
 event_id INT REFERENCES hie_events(event_id),
 message_type VARCHAR(10) NOT NULL,
 visit_type VARCHAR(5) DEFAULT 'ALL' NOT NULL,
 version VARCHAR(10) NOT NULL,
 priority INT DEFAULT 0 NOT NULL,
 center_id INT DEFAULT 0,
 interface_id INT REFERENCES interface_config_master(interface_id),
 status CHARACTER(1) DEFAULT 'A',
 rule_id INT,
 created_at TIMESTAMP DEFAULT now(),
 created_by VARCHAR(30) NOT NULL,
 modified_at TIMESTAMP DEFAULT now(),
 modified_by VARCHAR(30) NOT NULL,
 PRIMARY KEY (event_mapping_id),
 CHECK(visit_type IN ('OP','IP','OSP','ALL')),
 UNIQUE (event_id,visit_type,priority,center_id,interface_id)
);
COMMENT ON table interface_event_mapping is '{ "type": "Master", "comment": "Interface to event and message mapping" }';

-- Table of message details
CREATE SEQUENCE message_mapping_details_hl7_seq START 1;
COMMENT ON SEQUENCE message_mapping_details_hl7_seq IS '{ "type": "Master", "comment": "Holds sequence for message_segment_mapping_hl7_seq" }';

CREATE TABLE message_mapping_details_hl7 (
 id INT DEFAULT nextval('message_mapping_details_hl7_seq'),
 message_type VARCHAR(8) NOT NULL,
 segment VARCHAR(4) NOT NULL,
 version VARCHAR(10) NOT NULL,
 seg_order INT DEFAULT 0 NOT NULL,
 repeat_segment boolean DEFAULT false,
 status CHARACTER(1),
 interface_id INT REFERENCES interface_config_master(interface_id),
 created_at TIMESTAMP DEFAULT now(),
 created_by VARCHAR(30) NOT NULL,
 modified_at TIMESTAMP DEFAULT now(),
 modified_by VARCHAR(30) NOT NULL,
 PRIMARY KEY (id)
);
COMMENT ON table message_mapping_details_hl7 is '{ "type": "Master", "comment": "Message Segment Mappings HL7" }';

-- Table to store segment templates.
CREATE SEQUENCE hl7_segment_template_seq START 1;
COMMENT ON SEQUENCE hl7_segment_template_seq IS '{ "type": "Master", "comment": "Holds sequence for segment templates hl7 seq" }';

CREATE TABLE hl7_segment_template (
 seg_template_id INT DEFAULT nextval('hl7_segment_template_seq'),
 segment_field_number VARCHAR(10),
 interface_id INT REFERENCES interface_config_master(interface_id),
 version VARCHAR(10),
 status CHARACTER(1) DEFAULT 'A',
 field_template TEXT,
 created_at TIMESTAMP DEFAULT now(),
 created_by VARCHAR(30) NOT NULL,
 modified_at TIMESTAMP DEFAULT now(),
 modified_by VARCHAR(30) NOT NULL,
 PRIMARY KEY (seg_template_id),
 UNIQUE (segment_field_number,interface_id,version)
);
COMMENT ON table hl7_segment_template is '{ "type": "Master", "comment": "Holds template of hl7 segment" }';

-- Table to store event rules.
CREATE SEQUENCE interface_event_rule_master_seq START 1;
COMMENT ON SEQUENCE interface_event_rule_master_seq IS '{ "type": "Master", "comment": "Holds sequence for interface_event_rule_master seq" }';

CREATE TABLE interface_event_rule_master (
 rule_id INT DEFAULT nextval('interface_event_rule_master_seq'),
 rule_name VARCHAR(30),
 status CHARACTER(1) DEFAULT 'A',
 include TEXT DEFAULT '{}',
 exclude TEXT DEFAULT '{}',
 created_at TIMESTAMP DEFAULT now(),
 created_by VARCHAR(30) NOT NULL,
 modified_at TIMESTAMP DEFAULT now(),
 modified_by VARCHAR(30) NOT NULL,
 PRIMARY KEY (rule_id)
);
COMMENT ON table interface_event_rule_master is '{ "type": "Master", "comment": "Holds interface_event_rule_master data" }';

-- Obsolete unused tables
ALTER TABLE interface_hl7 RENAME TO obsolete_interface_hl7;
ALTER TABLE interface_details_hl7 RENAME TO obsolete_interface_details_hl7;
ALTER TABLE message_mapping_hl7 RENAME TO obsolete_message_mapping_hl7;
ALTER TABLE message_segment_mapping_hl7 RENAME TO obsolete_message_segment_mapping_hl7;
ALTER TABLE message_segments_hl7 RENAME TO obsolete_message_segments_hl7;
ALTER TABLE messages_hl7 RENAME TO obsolete_messages_hl7;
ALTER TABLE events_hl7 rename TO obsolete_events_hl7;