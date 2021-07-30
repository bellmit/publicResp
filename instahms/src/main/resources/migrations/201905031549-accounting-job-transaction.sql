-- liquibase formatted sql
-- changeset satishl2772:rename-joblogid-column-to-jobtransaction-accounting-table

CREATE SEQUENCE job_transaction_seq
START WITH 1
INCREMENT BY 1
NO MINVALUE
NO MAXVALUE
CACHE 1;

COMMENT ON SEQUENCE job_transaction_seq IS '{ "type": "Txn", "comment": "using to update job_transaction for hms_accounting_info" }';

SELECT setval('job_transaction_seq', COALESCE((SELECT MAX(job_log_id)+1 FROM hms_accounting_info), 1), true);

ALTER TABLE hms_accounting_info RENAME COLUMN job_log_id TO job_transaction;

COMMENT ON COLUMN hms_accounting_info.job_transaction IS 'stores the value from job_transaction_seq. It is useful as common transaction id per accounting job';
