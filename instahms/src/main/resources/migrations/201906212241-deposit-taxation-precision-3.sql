-- liquibase formatted sql
-- changeset mancini2802:deposit-taxation-precision-3 context:precision-3



ALTER TABLE receipts ALTER COLUMN total_tax_rate TYPE NUMERIC(16,3);	
ALTER TABLE receipt_tax ALTER COLUMN tax_rate TYPE NUMERIC(16,3);	
ALTER TABLE receipt_tax ALTER COLUMN tax_amount TYPE NUMERIC(16,3);