-- liquibase formatted sql

-- changeset shilpanr:submissionidwithcorrection-fix-hms-21080 failOnError:false

ALTER TABLE insurance_claim RENAME obsolete_submission_id_with_correction to submission_id_with_correction;
