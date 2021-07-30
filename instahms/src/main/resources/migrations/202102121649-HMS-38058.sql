-- liquibase formatted sql
-- changeset pranavpractoinsta:adding_person_register_related_tables failOnError:false

CREATE SEQUENCE person_register_submission_batch_seq;

CREATE TABLE person_register_submission_batch ( 
	personregister_batch_id VARCHAR DEFAULT nextval('person_register_submission_batch_seq') NOT NULL PRIMARY KEY,
	visit_type character(1) NOT NULL,
	account_group_id INTEGER NOT NULL,
	center_id integer NOT NULL DEFAULT 0,
	processing_status character(1) NOT NULL DEFAULT 'N',
	status character(1) NOT NULL DEFAULT 'O',
	batch_from_date timestamp NOT NULL,
	batch_to_date timestamp NOT NULL,
	file_name VARCHAR,
	errors character varying,
	created_at timestamp default NOW(),
	modified_at timestamp,
	submission_date timestamp,
	created_by character(25),
	modified_by character(30)
);
CREATE SEQUENCE person_register_submission_batch_details_seq;
CREATE TABLE person_register_submission_batch_details (
	personregister_batch_details_id INTEGER DEFAULT nextval('person_register_submission_batch_details_seq') NOT NULL PRIMARY KEY,
	personregister_batch_id VARCHAR NOT NULL REFERENCES person_register_submission_batch(personregister_batch_id),
	mr_no character(15)
);
COMMENT ON sequence person_register_submission_batch_seq is '{ "type": "Txn", "comment": "" }';
COMMENT ON table person_register_submission_batch is '{ "type": "Txn", "comment": "Person Register Submission" }';
COMMENT ON sequence person_register_submission_batch_details_seq is '{ "type": "Txn", "comment": "" }';
COMMENT ON table person_register_submission_batch_details is '{ "type": "Txn", "comment": "Person Register Submission Details" }';