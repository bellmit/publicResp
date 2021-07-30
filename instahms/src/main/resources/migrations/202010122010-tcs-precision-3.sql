-- liquibase formatted sql
-- changeset anandpatel:precision-3-changes context:precision-3
ALTER TABLE store_po_main ALTER COLUMN tcs_amount TYPE numeric(16,3); -- original: numeric(15,2)

ALTER TABLE store_invoice ALTER COLUMN tcs_amount TYPE numeric(16,3); -- original: numeric(15,2)
