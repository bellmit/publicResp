-- liquibase formatted sql
-- changeset harishm18:patient-package-content-charges-precision-3 context:precision-3
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:3 select after_decimal_digits from generic_preferences

alter table patient_package_content_charges alter column charge type numeric(16,3);
alter table patient_package_content_charges alter column discount type numeric(16,3);

alter table patient_customised_package_details alter column amount type numeric(16,3);
alter table patient_customised_package_details alter column discount type numeric(16,3);
