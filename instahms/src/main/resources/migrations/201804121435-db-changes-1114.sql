-- liquibase formatted sql
-- changeset deepakpracto:db-changes-1114.sql

-- db-changes-1114 for setting default value in some fields of bill_charge, 
-- getting wrong value in bill charge adjustment report builder, becouse of no default value for these fields.
 
ALTER TABLE bill_charge ALTER COLUMN dr_discount_amt SET DEFAULT 0.00;
ALTER TABLE bill_charge ALTER COLUMN pres_dr_discount_amt SET DEFAULT 0.00;
ALTER TABLE bill_charge ALTER COLUMN ref_discount_amt SET DEFAULT 0.00;
ALTER TABLE bill_charge ALTER COLUMN hosp_discount_amt SET DEFAULT 0.00;
ALTER TABLE bill_charge ALTER COLUMN prescribing_dr_amount SET DEFAULT 0.00;
ALTER TABLE bill_charge ALTER COLUMN referal_amount SET DEFAULT 0.00;