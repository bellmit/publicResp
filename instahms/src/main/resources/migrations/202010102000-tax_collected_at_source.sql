-- liquibase formatted sql
-- changeset anandpatel:adding column related to tax collected at source for stock entry

ALTER TABLE supplier_master ADD COLUMN tcs_applicable character(1) not null DEFAULT 'N';

ALTER TABLE store_po_main
ADD COLUMN tcs_type character(1) DEFAULT 'A',
ADD COLUMN tcs_per numeric DEFAULT 0,
ADD COLUMN tcs_amount numeric(15,2) DEFAULT 0;

ALTER TABLE store_invoice
ADD COLUMN tcs_type character(1) DEFAULT 'A',
ADD COLUMN tcs_per numeric DEFAULT 0,
ADD COLUMN tcs_amount numeric(15,2) DEFAULT 0;
