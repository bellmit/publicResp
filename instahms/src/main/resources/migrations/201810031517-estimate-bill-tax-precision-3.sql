-- liquibase formatted sql
-- changeset raj-nt:adding-estiamate-charge-tax-precision-3-changes context:precision-3
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:3 select after_decimal_digits from generic_preferences
-- validCheckSum: ANY

ALTER TABLE estimate_charge ALTER COLUMN tax_amt TYPE numeric(16,3);
ALTER TABLE estimate_charge ALTER COLUMN sponsor_tax TYPE numeric(16,3);
ALTER TABLE estimate_charge ALTER COLUMN patient_tax TYPE numeric(16,3);
