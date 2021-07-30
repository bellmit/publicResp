-- liquibase formatted sql
-- changeset rajendratalekar:supplier-returns-with-debit-note-csv-cron-insert

INSERT INTO cron_job (job_id, job_group, job_name, job_time, job_params, job_mod_dependency, job_allow_disable, job_status, job_next_runtime, job_last_runtime, job_last_status, job_description) VALUES (nextval('cron_job_seq'), 'SupplierReturnsWithDebitNoteCsvExporterJob', 'SupplierReturnsWithDebitNoteCsvExporterJob', '0 */15 * * * ?', NULL, NULL, 'Y', 'I', NULL, NULL, NULL, '00:15:00 Minutes:Store Debit Transactions will be imported every 15 mins automatically');
