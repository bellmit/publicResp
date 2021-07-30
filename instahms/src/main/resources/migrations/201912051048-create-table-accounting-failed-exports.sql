-- liquibase formatted sql
-- changeset rajendratalekar:create-table-accounting-failed-exports

CREATE TABLE accounting_failed_exports (
	id SERIAL UNIQUE,
	visit_id VARCHAR(15) NOT NULL,
	bill_no VARCHAR(15) NOT NULL,
	last_run timestamp without time zone NOT NULL
);

SELECT comment_on_table_or_sequence_if_exists('accounting_failed_exports',true, 'Txn','Log of failed accounting exports');
SELECT comment_on_table_or_sequence_if_exists('accounting_failed_exports_id_seq', false, 'Txn','');