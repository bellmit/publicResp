-- liquibase formatted sql

-- changeset SirishaRL:setting_default_0
ALTER TABLE bill_charge_claim alter column insurance_claim_amt set default 0.00;
