-- liquibase formatted sql
-- changeset harishm18:master-charges-cron-details-precision-3 context:precision-3
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:3 select after_decimal_digits from generic_preferences

alter table master_charges_cron_scheduler_details alter column charge type numeric(16,3);

alter table master_charges_cron_scheduler_details alter column discount type numeric(16,3);
