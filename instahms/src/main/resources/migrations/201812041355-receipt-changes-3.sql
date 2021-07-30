-- liquibase formatted sql
-- changeset qwewrty1:Receipt-changes context:precision-3

ALTER TABLE receipts ALTER COLUMN amount TYPE NUMERIC(16,3),
    ALTER COLUMN tds_amount TYPE NUMERIC(16,3),
    ALTER COLUMN exchange_rate TYPE NUMERIC(16,3),
    ALTER COLUMN other_deductions TYPE NUMERIC(16,3),
    ALTER COLUMN credit_card_commission_percentage TYPE NUMERIC(16,3),
    ALTER COLUMN credit_card_commission_amount TYPE NUMERIC(16,3),
    ALTER COLUMN currency_amt TYPE NUMERIC(16,3),
    ALTER COLUMN unallocated_amount TYPE NUMERIC(16,3);
ALTER TABLE bill_receipts ALTER COLUMN allocated_amount TYPE NUMERIC(16,3);
ALTER TABLE bill_charge_receipt_allocation ALTER COLUMN allocated_amount TYPE NUMERIC(16,3);
ALTER TABLE receipt_refund_reference ALTER COLUMN AMOUNT TYPE NUMERIC(16,3);
