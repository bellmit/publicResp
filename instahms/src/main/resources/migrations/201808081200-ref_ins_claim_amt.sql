-- liquibase formatted sql

-- changeset irshadmohammad:ref_ins_claim_amt.sql splitStatements:false

-- === added new column for ref_insurance_claim_amount changes ==== 
ALTER TABLE sales_claim_details ADD COLUMN ref_insurance_claim_amount numeric(15,4);