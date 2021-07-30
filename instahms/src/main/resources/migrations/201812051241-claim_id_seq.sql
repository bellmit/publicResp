-- liquibase formatted sql
-- changeset shilpanr:claim-id-sequence-at-account-group-level

ALTER TABLE hosp_claim_seq_prefs ADD COLUMN account_group integer DEFAULT 0;
