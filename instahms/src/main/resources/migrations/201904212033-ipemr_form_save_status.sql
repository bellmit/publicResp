-- liquibase formatted sql
-- changeset sonam009:ipemr-form-save-status-and-reopen-reason

ALTER TABLE patient_registration ADD COLUMN ipemr_reopen_remarks character varying(2000);
COMMENT ON COLUMN patient_registration.ipemr_reopen_remarks IS 'Reopen remarks column for IP EMR form';

ALTER TABLE patient_registration ADD COLUMN ipemr_status character(1) default 'N';
COMMENT ON COLUMN patient_registration.ipemr_status is 'N -> Not at started, P -> Partially saved (saved atlest once), C -> Done';

