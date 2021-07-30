-- liquibase formatted sql
-- changeset vishwas07:service-resource-mapping

CREATE SEQUENCE service_service_resources_mapping_seq
        START WITH 1
        INCREMENT BY 1
        NO MINVALUE
        NO MAXVALUE
        CACHE 1;

CREATE TABLE service_service_resources_mapping (
  service_service_resources_mapping_id INTEGER DEFAULT nextval('service_service_resources_mapping_seq'::regclass) NOT NULL,
  service_id VARCHAR(10) NOT NULL,
  serv_res_id INTEGER NOT NULL,
  created_at TIMESTAMP DEFAULT NOW(),
  PRIMARY KEY(service_service_resources_mapping_id),
  UNIQUE (service_id, serv_res_id)
);

COMMENT ON table service_service_resources_mapping is '{ "type": "Txn", "comment": "Holds service and service resource mapped data" }';

COMMENT ON SEQUENCE service_service_resources_mapping_seq is '{ "type": "Txn", "comment": "Holds service and service resource mapped sequence" }';
