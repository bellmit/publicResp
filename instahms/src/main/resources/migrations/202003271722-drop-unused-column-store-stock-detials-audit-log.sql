-- liquibase formatted sql
-- changeset rajendratalekar:drop-unused-column-store-stock-detials-audit-log
ALTER TABLE store_stock_details_audit_log DROP COLUMN record_type;