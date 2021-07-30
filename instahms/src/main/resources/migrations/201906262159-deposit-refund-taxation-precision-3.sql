-- liquibase formatted sql
-- changeset mancini2802:deposit-refund-taxation-precision-3 context:precision-3
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:3 select after_decimal_digits from generic_preferences

alter table receipt_refund_reference alter column tax_amount type numeric(16,3);

alter table receipt_refund_reference alter column tax_rate type numeric(16,3);