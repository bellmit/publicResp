-- liquibase formatted sql
-- changeset vakul-practo:create-data-backload-table-structure-and-related-migration-scripts

CREATE SEQUENCE backload_audit_table_seq START 1;
COMMENT ON SEQUENCE backload_audit_table_seq IS '{ "type": "Txn", "comment": "Holds sequence for Backload audit table" }';

CREATE TABLE backload_audit_table (
 backload_audit_id INT DEFAULT nextval('backload_audit_table_seq'),
 center_id INT,
 interface_id INT,
 job_key character varying(100),
 records_found BIGINT,
 records_processed BIGINT,
 record_start_date character varying(20),
 record_end_date character varying(20),
 job_submitted_time timestamp without time zone DEFAULT now(),
 status character varying(100) DEFAULT 'INITIATED',
 created_by character varying(100) DEFAULT 'InstaAdmin',
 created_at timestamp without time zone DEFAULT now(),
 PRIMARY KEY (backload_audit_id)
);
COMMENT ON TABLE backload_audit_table IS '{ "type": "Txn", "comment": "Backload Job audit table" }';

-- END OF MIGRATION --
