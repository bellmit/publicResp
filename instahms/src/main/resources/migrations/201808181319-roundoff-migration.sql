-- liquibase formatted sql

-- changeset irshadmohammad:201808181319-roundoff-migration.sql splitStatements:false

-- === migration script to update ref_insurance_claim_amount with insurance_claim_amt ==== 
UPDATE sales_claim_details SET ref_insurance_claim_amount = insurance_claim_amt;