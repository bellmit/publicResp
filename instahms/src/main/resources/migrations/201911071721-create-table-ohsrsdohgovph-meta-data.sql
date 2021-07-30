-- liquibase formatted sql
-- changeset rajendratalekar:create-table-ohsrsdohgovph-meta-data

CREATE TABLE ohsrsdohgovph_meta_data (
	id SERIAL UNIQUE,
	ohsrs_function varchar(200) NOT NULL,
	field varchar(200) NOT NULL,
	value character varying(500),
	description character varying(500)
);

SELECT comment_on_table_or_sequence_if_exists('ohsrsdohgovph_meta_data',true, 'Master','ohsrs.doh.gov.ph meta data');
SELECT comment_on_table_or_sequence_if_exists('ohsrsdohgovph_meta_data_id_seq', false, 'Master','');
