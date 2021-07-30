-- liquibase formatted sql
-- changeset javalkarvinay:state_master_table_change_for_hl7_message

ALTER TABLE state_master ADD COLUMN id serial;
SELECT comment_on_table_or_sequence_if_exists('state_master_id_seq', false, 'Master','');

INSERT INTO code_system_categories VALUES (7,'state_master');
