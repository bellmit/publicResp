-- liquibase formatted sql
-- changeset rajendratalekar:create-table-ohsrsdohgovph-report-data

CREATE TABLE ohsrsdohgovph_report_data (
	id SERIAL UNIQUE,
	center_id int REFERENCES hospital_center_master(center_id) NOT NULL,
	reporting_year int,
	ohsrs_function varchar(200) NOT NULL,
	field varchar(200) NOT NULL,
	value character varying(500),
	table_index bigint,
	upload boolean,
    created_by character varying(50) REFERENCES u_user(emp_username) NOT NULL,
    created_at timestamp without time zone DEFAULT now(),
    modified_by character varying(50) REFERENCES u_user(emp_username) NOT NULL,
    modified_at timestamp without time zone,
    status character(1)
);

SELECT comment_on_table_or_sequence_if_exists('ohsrsdohgovph_report_data',true, 'Txn','ohsrs.doh.gov.ph report data');
SELECT comment_on_table_or_sequence_if_exists('ohsrsdohgovph_report_data_id_seq', false, 'Txn','');

CREATE SEQUENCE ohsrsdohgovph_report_data_table_index_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

SELECT comment_on_table_or_sequence_if_exists('ohsrsdohgovph_report_data_table_index_seq', false, 'Txn','');
