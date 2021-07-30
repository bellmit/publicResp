-- liquibase formatted sql
-- changeset rajendratalekar:new-cron-queue-accounting-missing-bill-job

INSERT INTO cron_job (job_id, job_group, job_name, job_time, job_params, job_mod_dependency, job_allow_disable, job_status, job_next_runtime, job_last_runtime, job_last_status, job_description) VALUES (nextval('cron_job_seq'), 'AccountingQueueMissingBillsJob', 'AccountingQueueMissingBillsJob', '0 0 * * * ?', NULL, NULL, 'Y', 'A', NULL, NULL, NULL, 'Findmissing bills in accounting info table every 1 hour');
