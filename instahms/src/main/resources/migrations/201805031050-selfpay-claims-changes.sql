-- liquibase formatted sql
-- changeset deepakpracto:claim-generation-changes.sql splitStatements:false

-- === added new tables for selfpay claim generation changes ==== 

CREATE TABLE selfpay_sponsor_types (
	sponsor_type_id INTEGER NOT NULL REFERENCES sponsor_type(sponsor_type_id)
);

CREATE SEQUENCE selfpay_submission_batch_seq;

CREATE TABLE selfpay_submission_batch ( 
	selfpay_batch_id INTEGER DEFAULT nextval('selfpay_submission_batch_seq') NOT NULL PRIMARY KEY,
	visit_type character(1) NOT NULL,
	account_group_id INTEGER NOT NULL REFERENCES account_group_master(account_group_id),
	center_id integer NOT NULL DEFAULT 0,
	processing_status character(1) NOT NULL DEFAULT 'N',
	selfpay_status character(1) NOT NULL DEFAULT 'O',
	batch_from_date timestamp NOT NULL,
	batch_to_date timestamp NOT NULL,
	file_name character(50),
	errors character varying,
	created_at timestamp default NOW(),
	modified_at timestamp,
	submission_date timestamp,
	created_by character(25),
	modified_by character(30)
);

ALTER TABLE bill ADD COLUMN selfpay_batch_id INTEGER DEFAULT 0 NOT NULL;
