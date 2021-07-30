-- liquibase formatted sql
-- changeset rajendratalekar:new-cron-accouting-failed-export-retry-job

INSERT INTO cron_job (job_id, job_group, job_name, job_time, job_params, job_mod_dependency, job_allow_disable, job_status, job_next_runtime, job_last_runtime, job_last_status, job_description) VALUES (nextval('cron_job_seq'), 'AccountingFailedExportRetryJob', 'AccountingFailedExportRetryJob', '0 */15 * * * ?', NULL, NULL, 'Y', 'A', NULL, NULL, NULL, 'Retry posting of failed accounting exports every 15 mins');
