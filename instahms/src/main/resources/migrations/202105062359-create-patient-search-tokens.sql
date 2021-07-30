-- liquibase formatted sql
-- changeset sreenivasayashwanth:create-patient-search-tokens-table

create table patient_search_tokens (entity_id character varying(100),entity character varying(100),token character varying(100), reversed boolean default false);

SELECT comment_on_table_or_sequence_if_exists('patient_search_tokens',true, 'Txn','Search Tokens for Patient and Contacts');
