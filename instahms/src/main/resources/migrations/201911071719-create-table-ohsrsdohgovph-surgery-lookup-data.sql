-- liquibase formatted sql
-- changeset rajendratalekar:create-table-ohsrsdohgovph-icd-lookup-data

CREATE TABLE ohsrsdohgovph_surgery_lookup_data (
	id SERIAL UNIQUE,
	operationcode character varying(10),
	description character varying(500)
);

SELECT comment_on_table_or_sequence_if_exists('ohsrsdohgovph_surgery_lookup_data',true, 'Master','ohsrs.doh.gov.ph surgery lookup data');
SELECT comment_on_table_or_sequence_if_exists('ohsrsdohgovph_surgery_lookup_data_id_seq', false, 'Master','');
