-- liquibase formatted sql
-- changeset rajendratalekar:rename-table-outbound-referral-reason-master

ALTER TABLE outbound_referral_reason_master RENAME TO reason_for_referral;

ALTER SEQUENCE outbound_referral_reason_master_id_seq RENAME TO reason_for_referral_seq;