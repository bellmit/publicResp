-- liquibase formatted sql
-- changeset manika.singh:<add-submission-batch-type-in-bill-charge>
ALTER TABLE bill_charge ADD COLUMN submission_batch_type character varying(1);
