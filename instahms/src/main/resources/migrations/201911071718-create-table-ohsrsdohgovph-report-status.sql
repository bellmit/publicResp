-- liquibase formatted sql
-- changeset rajendratalekar:create-table-ohsrsdohgovph-report-status

CREATE TABLE ohsrsdohgovph_report_status (
	id SERIAL UNIQUE,
	center_id int REFERENCES hospital_center_master(center_id) NOT NULL,
	reporting_year int,
	ohsrs_function varchar(200) NOT NULL,
	status character varying(50),
    details text,
    created_by character varying(50) REFERENCES u_user(emp_username) NOT NULL,
    created_at timestamp without time zone DEFAULT now(),
    modified_by character varying(50) REFERENCES u_user(emp_username) NOT NULL,
    modified_at timestamp without time zone
);

SELECT comment_on_table_or_sequence_if_exists('ohsrsdohgovph_report_status',true, 'Txn','ohsrs.doh.gov.ph report status');
SELECT comment_on_table_or_sequence_if_exists('ohsrsdohgovph_report_status_id_seq', false, 'Txn','');
