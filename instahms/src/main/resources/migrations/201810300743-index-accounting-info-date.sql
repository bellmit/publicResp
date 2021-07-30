-- liquibase formatted sql
-- changeset rajendratalekar:index-accounting-info-date failOnError:false

create index hai_bill_open_date on hms_accounting_info (bill_open_date);
create index hai_bill_finalized_date on hms_accounting_info (bill_finalized_date);
