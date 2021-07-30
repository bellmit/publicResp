-- liquibase formatted sql
-- changeset manasaparam:adding-index-on-voucher-date failOnError:false

CREATE INDEX hai_voucher_date_idx ON hms_accounting_info (voucher_date);