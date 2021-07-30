-- liquibase formatted sql
-- changeset satishl2772:adding-new-column-to-hms-accounting-info-table

ALTER TABLE hms_accounting_info ADD COLUMN job_log_id integer;

COMMENT ON COLUMN hms_accounting_info.job_log_id IS 'storing the job_log_id from job_log table';

ALTER TABLE hms_accounting_info ADD COLUMN created_at timestamp without time zone DEFAULT now();
