-- liquibase formatted sql
-- changeset tejakilaru:<new-triage-summary-changes>

ALTER TABLE doctor_consultation ADD COLUMN triage_start_datetime timestamp without time zone;
ALTER TABLE doctor_consultation ADD COLUMN triage_end_datetime timestamp without time zone;

COMMENT ON COLUMN doctor_consultation.triage_done is 'N -> Not at started, P -> Partially saved (saved atlest once), Y -> Done';
