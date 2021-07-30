-- liquibase formatted sql
-- changeset vinaykumarjavalkar:store_grn_details_alter_column_cost_value context:precision-3
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:3 select after_decimal_digits from generic_preferences
-- validCheckSum: ANY

ALTER TABLE store_grn_details ALTER COLUMN cost_value TYPE numeric(16,3);