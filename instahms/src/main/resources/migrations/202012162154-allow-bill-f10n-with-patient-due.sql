-- liquibase formatted sql
-- changeset anandpatel:added-column-allow_bill_fnlz_with_pat_due-in-u_user-table

ALTER TABLE u_user ADD COLUMN allow_bill_fnlz_with_pat_due character(1) DEFAULT 'Y'::bpchar;
