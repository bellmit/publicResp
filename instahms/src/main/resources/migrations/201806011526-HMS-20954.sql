-- liquibase formatted sql
-- changeset SirishaRL:migrate_null_claim_amt_to_zero
update bill_charge_claim set insurance_claim_amt = 0.00 where insurance_claim_amt is null;