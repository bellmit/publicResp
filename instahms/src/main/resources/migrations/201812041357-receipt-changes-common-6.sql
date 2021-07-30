-- liquibase formatted sql
-- changeset allabakash:Receipt-changes-common-6
-- validCheckSum: ANY
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:t SELECT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = ANY (SELECT unnest(string_to_array(setting, ', '))  FROM pg_settings WHERE name = 'search_path') AND table_name='bill_receipts' AND column_name='payment_type')

CREATE INDEX bill_receipts_payment_type_idx ON bill_receipts(payment_type);