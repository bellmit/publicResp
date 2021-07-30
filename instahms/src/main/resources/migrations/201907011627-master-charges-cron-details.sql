-- liquibase formatted sql
-- changeset harishm18:master-charges-cron-details

CREATE SEQUENCE master_charges_cron_scheduler_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE master_charges_cron_scheduler_details (
	id INTEGER DEFAULT nextval('master_charges_cron_scheduler_id_seq'::regclass) NOT NULL PRIMARY KEY,
    entity_id character varying(50)  NOT NULL,
    entity character varying(100) NOT NULL,
    charge numeric(15, 2) NOT NULL,
    discount numeric(15, 2) NOT NULL,
    status character varying(1),
    error_message character varying(1000),
    scheduled_timestamp timestamp without time zone DEFAULT NOW()
);

COMMENT ON TABLE master_charges_cron_scheduler_details IS 
	'{ "type": "Txn", "comment": "Contains details of the scheduled master charges creation job." }';

COMMENT ON COLUMN master_charges_cron_scheduler_details.status IS 'Applicable scheduler status: P(Processing)/F(Failed)/S(Success)';
COMMENT ON COLUMN master_charges_cron_scheduler_details.error_message IS 'Applicable scheduler error message, if job failed';
COMMENT ON SEQUENCE master_charges_cron_scheduler_id_seq IS 
	'{ "type": "Txn", "comment": "Sequence ID of the scheduled master charges creation job." }';
