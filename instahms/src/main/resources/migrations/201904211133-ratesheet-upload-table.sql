-- liquibase formatted sql
-- changeset mohamedanees:rate-sheet-upload-table

CREATE SEQUENCE rate_sheet_creation_scheduler_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE rate_sheet_creation_scheduler_details (
	id INTEGER DEFAULT nextval('rate_sheet_creation_scheduler_id_seq'::regclass) NOT NULL PRIMARY KEY,
    org_id character varying(10),
    org_name character varying(100) NOT NULL,
    status character varying(1),
    error_message character varying(500),
    scheduled_timestamp timestamp without time zone DEFAULT NOW()
);

COMMENT ON TABLE rate_sheet_creation_scheduler_details IS 
	'{ "type": "Txn", "comment": "Contains details of the scheduled rate sheet creation job." }';

COMMENT ON COLUMN rate_sheet_creation_scheduler_details.status IS 'Applicable scheduler status: P(Processing)/F(Failed)/S(Success)';
COMMENT ON COLUMN rate_sheet_creation_scheduler_details.error_message IS 'Applicable scheduler error message, if job failed';
COMMENT ON SEQUENCE rate_sheet_creation_scheduler_id_seq IS 
	'{ "type": "Txn", "comment": "Sequence ID of the scheduled rate sheet creation job." }';