-- liquibase formatted sql
-- changeset allabakash:Receipt-changes-common-6-1
-- validCheckSum: ANY
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:f select count(*) > 0 from pg_indexes where schemaname = ANY (SELECT unnest(string_to_array(setting, ', '))  FROM pg_settings WHERE name = 'search_path') and indexname='idx_receipt_refund_reference_receipt_id'

CREATE INDEX idx_receipt_refund_reference_receipt_id ON receipt_refund_reference(receipt_id);
