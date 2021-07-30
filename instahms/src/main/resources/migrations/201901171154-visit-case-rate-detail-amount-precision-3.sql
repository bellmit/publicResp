-- liquibase formatted sql
-- changeset shilpanr:visit-case-rate-detail-amount-precision-3-changes context:precision-3
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:3 select after_decimal_digits from generic_preferences
-- validCheckSum: ANY

ALTER TABLE visit_case_rate_detail ALTER COLUMN amount TYPE numeric(16,3);