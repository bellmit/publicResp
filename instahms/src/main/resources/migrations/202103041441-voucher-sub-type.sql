-- liquibase formatted sql
-- changeset manasaparam:adding-voucher-sub-type-in-accounting

ALTER TABLE hms_accounting_info ADD COLUMN voucher_sub_type  character varying(50);