-- liquibase formatted sql
-- changeset pallavia08:alter-tables-queries-for-receipts-table-for-salucro-integration
ALTER TABLE receipts ADD column payment_transaction_id Integer;