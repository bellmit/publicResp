-- liquibase formatted sql
-- changeset allabakash:bill-receipts-receipt-no-index

CREATE INDEX bill_receipts_receipt_no_index on bill_receipts(receipt_no);
