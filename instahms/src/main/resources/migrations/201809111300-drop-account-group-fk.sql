-- liquibase formatted sql
-- changeset junzy:drops-uneeded-fk-constraint

ALTER  TABLE selfpay_submission_batch DROP constraint selfpay_submission_batch_account_group_id_fkey;
