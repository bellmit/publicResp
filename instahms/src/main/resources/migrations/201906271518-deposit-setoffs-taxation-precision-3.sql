-- liquibase formatted sql
-- changeset mancini2802:deposit-taxation-presicion-3 context:precision-3
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:3 select after_decimal_digits from generic_preferences

alter table deposit_setoff_total alter column hosp_total_tax_amount type numeric(16,3);

alter table deposit_setoff_total alter column hosp_total_setoffs_tax_amount type numeric(16,3) ;
