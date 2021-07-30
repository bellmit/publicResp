-- liquibase formatted sql
-- changeset satishl2772:adding-sale-bill-no-column-to-hms-accounting-info-table

ALTER TABLE hms_accounting_info ADD COLUMN sale_bill_no character varying(15);

COMMENT ON COLUMN hms_accounting_info.sale_bill_no IS 'storing the sale_id from store_sales_main table';
