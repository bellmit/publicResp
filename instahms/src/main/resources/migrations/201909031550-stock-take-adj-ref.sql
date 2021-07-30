-- liquibase formatted sql
-- changeset anupamamr:stock-take-stock-adj-xref

ALTER TABLE store_adj_main ADD COLUMN stock_take_id character varying(15);

