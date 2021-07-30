-- liquibase formatted sql
-- changeset anandpatel:adding-column-external_transaction_id-for-csv-inbound
alter table store_transfer_main add column external_transaction_id character varying(100);
alter table store_debit_note add column external_transaction_id character varying(100);
alter table store_adj_main add column external_transaction_id character varying(100);
