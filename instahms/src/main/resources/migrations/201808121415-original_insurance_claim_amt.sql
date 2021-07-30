-- liquibase formatted sql

-- changeset irshadmohammad:original_insurance_claim_amt.sql splitStatements:false

-- === added new column for ref_insurance_claim_amount changes ==== 
ALTER TABLE sales_claim_details ADD COLUMN org_insurance_claim_amount numeric(15,4);