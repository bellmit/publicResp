-- liquibase formatted sql
-- changeset manasaparam:operation-theater-mapping

CREATE SEQUENCE operation_theatre_mapping_seq
        START WITH 1
        INCREMENT BY 1
        NO MINVALUE
        NO MAXVALUE
        CACHE 1;
        
CREATE TABLE operation_theatre_mapping (
  operation_theatre_mapping_id INTEGER DEFAULT nextval('operation_theatre_mapping_seq'::regclass) NOT NULL,
  operation_id VARCHAR(10) NOT NULL,
  theatre_id VARCHAR(10) NOT NULL,
  created_at TIMESTAMP DEFAULT NOW(),
  PRIMARY KEY(operation_theatre_mapping_id),
  UNIQUE (operation_id, theatre_id)
);

COMMENT ON table operation_theatre_mapping is '{ "type": "Master", "comment": "Holds operation and theatre mapped data" }';

COMMENT ON SEQUENCE operation_theatre_mapping_seq is '{ "type": "Master", "comment": "Holds operation and theatre mapped sequence" }';
