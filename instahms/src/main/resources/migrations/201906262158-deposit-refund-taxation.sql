-- liquibase formatted sql
-- changeset mancini2802:deposit-refund-taxation

alter table receipt_refund_reference add column tax_amount numeric(15,2) default 0.00;

alter table receipt_refund_reference add column tax_rate numeric(15,2) default 0.00;