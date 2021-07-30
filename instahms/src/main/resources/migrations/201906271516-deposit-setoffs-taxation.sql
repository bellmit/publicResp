-- liquibase formatted sql
-- changeset mancini2802:deposit-setoffs-taxation

alter table deposit_setoff_total add column hosp_total_tax_amount numeric(15,2) default 0.00;

alter table deposit_setoff_total add column hosp_total_setoffs_tax_amount numeric(15,2) default 0.00;