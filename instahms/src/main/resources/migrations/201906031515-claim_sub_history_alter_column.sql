-- liquibase formatted sql
-- changeset shilpanr:claim-submission-history-receiverid-type-change

ALTER TABLE claim_submission_history ALTER COLUMN sender_id TYPE character varying(200);
ALTER TABLE claim_submission_history ALTER COLUMN receiver_id TYPE character varying(200);
