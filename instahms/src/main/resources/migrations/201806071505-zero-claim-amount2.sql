-- liquibase formatted sql
-- changeset deepakpracto:zero-claim-amount2

-- === Add new column for already for zero e-claim generation changes ==== 

ALTER TABLE bill_charge add column allow_zero_claim boolean DEFAULT false NOT NULL;
ALTER TABLE store_sales_details add column allow_zero_claim boolean DEFAULT false NOT NULL;
