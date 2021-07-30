-- liquibase formatted sql
-- changeset vinaykumarjavalkar:master_table_changes_for_hl7

ALTER TABLE department ADD COLUMN id serial;
ALTER TABLE country_master ADD COLUMN id serial;
SELECT comment_on_table_or_sequence_if_exists('department_id_seq', false, 'Master','');
SELECT comment_on_table_or_sequence_if_exists('country_master_id_seq', false, 'Master','');