-- liquibase formatted sql
-- changeset manjular:to_check_whether_batch_is_manually_marked_as_sent failOnError:false

ALTER TABLE insurance_submission_batch ADD COLUMN is_manual_sent_batch BOOLEAN DEFAULT FALSE;

