-- liquibase formatted sql
-- changeset allabakash:Receipt-changes-receipt-payment-type-drop-index
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:t select count(*) > 0 from pg_indexes where schemaname = ANY (SELECT unnest(string_to_array(setting, ', '))  FROM pg_settings WHERE name = 'search_path') and indexname='bill_receipts_payment_type_idx'

DROP INDEX bill_receipts_payment_type_idx;