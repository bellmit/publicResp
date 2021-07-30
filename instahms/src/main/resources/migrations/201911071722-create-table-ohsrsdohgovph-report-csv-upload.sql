-- liquibase formatted sql
-- changeset rajendratalekar:create-table-ohsrsdohgovph-report-csv-upload

CREATE TABLE ohsrsdohgovph_report_csv_upload (
	id SERIAL UNIQUE,
	center_id int REFERENCES hospital_center_master(center_id) NOT NULL,
	reporting_year int,
	ohsrs_function varchar(200) NOT NULL,
	processed boolean,
	content bytea,
    uploaded_by character varying(50) REFERENCES u_user(emp_username) NOT NULL,
    uploaded_at timestamp without time zone DEFAULT now(),
    reuploaded_by character varying(50) REFERENCES u_user(emp_username) NOT NULL,
    reuploaded_at timestamp without time zone,
    status character(1)
);

SELECT comment_on_table_or_sequence_if_exists('ohsrsdohgovph_report_csv_upload',true, 'Txn','ohsrs.doh.gov.ph uploaded data');
SELECT comment_on_table_or_sequence_if_exists('ohsrsdohgovph_report_csv_upload_id_seq', false, 'Txn','');
