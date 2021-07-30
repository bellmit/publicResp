-- liquibase formatted sql
-- changeset deepakpracto:bill-Label-For-Bill-Later-Bills.sql

ALTER TABLE generic_preferences ADD COLUMN bill_label_for_bill_later_bills character varying(1) NOT NULL DEFAULT 'N';
