-- liquibase formatted sql
-- changeset javalkarvinay:added_segment_configurability

CREATE TABLE messages_hl7 (
    id INTEGER PRIMARY KEY,
    message VARCHAR(10),
    version VARCHAR(5),
    status CHARACTER(1),
    UNIQUE(message,version)
);
COMMENT ON table messages_hl7 is '{ "type": "Master", "comment": "Messages HL7" }';

CREATE TABLE message_segments_hl7 (
    id INTEGER PRIMARY KEY,
    segment VARCHAR(10),
    version VARCHAR(5),
    status CHARACTER(1),
    UNIQUE(segment,version)
);
COMMENT ON table message_segments_hl7 is '{ "type": "Master", "comment": "Message Segment HL7" }';

CREATE SEQUENCE message_segment_mapping_hl7_seq START 1;
COMMENT ON SEQUENCE message_segment_mapping_hl7_seq IS '{ "type": "Txn", "comment": "Holds sequence for message_segment_mapping_hl7_seq" }';

CREATE TABLE message_segment_mapping_hl7 (
    id BIGINT DEFAULT nextval('message_segment_mapping_hl7_seq'),
	msg_id INTEGER REFERENCES messages_hl7(id),
    segment_id INTEGER REFERENCES message_segments_hl7(id),
    seg_order INTEGER default 0 NOT NULL,
    repeat_segment boolean default false,
    status CHARACTER(1),
    PRIMARY KEY (id)
);
COMMENT ON table message_segment_mapping_hl7 is '{ "type": "Txn", "comment": "Message Segment Mappings HL7" }';

INSERT INTO messages_hl7 (id,message,version,status) VALUES (1,'ADTA01','251','A');
INSERT INTO messages_hl7 (id,message,version,status) VALUES (2,'ADTA03','251','A');
INSERT INTO messages_hl7 (id,message,version,status) VALUES (3,'ADTA04','251','A');
INSERT INTO messages_hl7 (id,message,version,status) VALUES (4,'ADTA06','251','A');
INSERT INTO messages_hl7 (id,message,version,status) VALUES (5,'ADTA08','251','A');
INSERT INTO messages_hl7 (id,message,version,status) VALUES (6,'ADTA11','251','A');
INSERT INTO messages_hl7 (id,message,version,status) VALUES (7,'ADTA13','251','A');
INSERT INTO messages_hl7 (id,message,version,status) VALUES (8,'ADTA28','251','A');
INSERT INTO messages_hl7 (id,message,version,status) VALUES (9,'ADTA31','251','A');
INSERT INTO messages_hl7 (id,message,version,status) VALUES (10,'ADTA40','251','A');
INSERT INTO messages_hl7 (id,message,version,status) VALUES (11,'OMPO09','251','A');
INSERT INTO messages_hl7 (id,message,version,status) VALUES (12,'ORUR01','251','A');
INSERT INTO messages_hl7 (id,message,version,status) VALUES (13,'PPRPC1','251','A');
INSERT INTO messages_hl7 (id,message,version,status) VALUES (14,'PPRPC2','251','A');
INSERT INTO messages_hl7 (id,message,version,status) VALUES (15,'PPRPC3','251','A');
INSERT INTO messages_hl7 (id,message,version,status) VALUES (16,'RDSO13','251','A');

INSERT INTO message_segments_hl7 (id,segment,version,status) VALUES (1,'MSH','251','A');
INSERT INTO message_segments_hl7 (id,segment,version,status) VALUES (2,'EVN','251','A');
INSERT INTO message_segments_hl7 (id,segment,version,status) VALUES (3,'PID','251','A');
INSERT INTO message_segments_hl7 (id,segment,version,status) VALUES (4,'PV1','251','A');
INSERT INTO message_segments_hl7 (id,segment,version,status) VALUES (5,'PV2','251','A');
INSERT INTO message_segments_hl7 (id,segment,version,status) VALUES (6,'DG1','251','A');
INSERT INTO message_segments_hl7 (id,segment,version,status) VALUES (7,'IN1','251','A');
INSERT INTO message_segments_hl7 (id,segment,version,status) VALUES (8,'MRG','251','A');
INSERT INTO message_segments_hl7 (id,segment,version,status) VALUES (9,'AL1','251','A');
INSERT INTO message_segments_hl7 (id,segment,version,status) VALUES (10,'PRB','251','A');
INSERT INTO message_segments_hl7 (id,segment,version,status) VALUES (11,'ROL','251','A');
INSERT INTO message_segments_hl7 (id,segment,version,status) VALUES (12,'NTE','251','A');
INSERT INTO message_segments_hl7 (id,segment,version,status) VALUES (13,'PR1','251','A');
INSERT INTO message_segments_hl7 (id,segment,version,status) VALUES (14,'ORC','251','A');
INSERT INTO message_segments_hl7 (id,segment,version,status) VALUES (15,'RXO','251','A');
INSERT INTO message_segments_hl7 (id,segment,version,status) VALUES (16,'RXR','251','A');
INSERT INTO message_segments_hl7 (id,segment,version,status) VALUES (17,'RXD','251','A');
INSERT INTO message_segments_hl7 (id,segment,version,status) VALUES (18,'OBR','251','A');
INSERT INTO message_segments_hl7 (id,segment,version,status) VALUES (19,'OBX','251','A');

INSERT INTO message_segment_mapping_hl7 (msg_id,segment_id,seg_order,repeat_segment,status) VALUES (11,1,0,false,'A');
INSERT INTO message_segment_mapping_hl7 (msg_id,segment_id,seg_order,repeat_segment,status) VALUES (11,3,1,false,'A');
INSERT INTO message_segment_mapping_hl7 (msg_id,segment_id,seg_order,repeat_segment,status) VALUES (11,4,2,false,'A');
INSERT INTO message_segment_mapping_hl7 (msg_id,segment_id,seg_order,repeat_segment,status) VALUES (11,14,3,true,'A');
INSERT INTO message_segment_mapping_hl7 (msg_id,segment_id,seg_order,repeat_segment,status) VALUES (11,15,4,true,'A');
INSERT INTO message_segment_mapping_hl7 (msg_id,segment_id,seg_order,repeat_segment,status) VALUES (11,16,5,true,'A');

INSERT INTO message_segment_mapping_hl7 (msg_id,segment_id,seg_order,repeat_segment,status) VALUES (12,1,0,false,'A');
INSERT INTO message_segment_mapping_hl7 (msg_id,segment_id,seg_order,repeat_segment,status) VALUES (12,3,1,false,'A');
INSERT INTO message_segment_mapping_hl7 (msg_id,segment_id,seg_order,repeat_segment,status) VALUES (12,4,2,false,'A');
INSERT INTO message_segment_mapping_hl7 (msg_id,segment_id,seg_order,repeat_segment,status) VALUES (12,5,3,false,'A');
INSERT INTO message_segment_mapping_hl7 (msg_id,segment_id,seg_order,repeat_segment,status) VALUES (12,14,4,false,'A');
INSERT INTO message_segment_mapping_hl7 (msg_id,segment_id,seg_order,repeat_segment,status) VALUES (12,18,5,false,'A');
INSERT INTO message_segment_mapping_hl7 (msg_id,segment_id,seg_order,repeat_segment,status) VALUES (12,19,6,true,'A');

INSERT INTO message_segment_mapping_hl7 (msg_id,segment_id,seg_order,repeat_segment,status) VALUES (16,1,0,false,'A');
INSERT INTO message_segment_mapping_hl7 (msg_id,segment_id,seg_order,repeat_segment,status) VALUES (16,3,1,false,'A');
INSERT INTO message_segment_mapping_hl7 (msg_id,segment_id,seg_order,repeat_segment,status) VALUES (16,4,2,false,'A');
INSERT INTO message_segment_mapping_hl7 (msg_id,segment_id,seg_order,repeat_segment,status) VALUES (16,14,3,true,'A');
INSERT INTO message_segment_mapping_hl7 (msg_id,segment_id,seg_order,repeat_segment,status) VALUES (16,17,4,true,'A');
INSERT INTO message_segment_mapping_hl7 (msg_id,segment_id,seg_order,repeat_segment,status) VALUES (16,16,5,true,'A');