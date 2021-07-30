-- liquibase formatted sql
-- changeset rajendratalekar:create-table-ohsrsdohgovph-icd-lookup-data

CREATE TABLE ohsrsdohgovph_icd_lookup_data (
	id SERIAL UNIQUE,
	icd10code character varying(20),
	icd10desc character varying(500),
	icd10cat character varying(50)
);

SELECT comment_on_table_or_sequence_if_exists('ohsrsdohgovph_icd_lookup_data',true, 'Master','ohsrs.doh.gov.ph icd lookup data');
SELECT comment_on_table_or_sequence_if_exists('ohsrsdohgovph_icd_lookup_data_id_seq', false, 'Master','');
