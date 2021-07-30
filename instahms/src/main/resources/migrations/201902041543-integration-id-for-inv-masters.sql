-- liquibase formatted sql
-- changeset adeshatole:integration-id-for-inv-masters


INSERT INTO cron_job (job_id, job_group, job_name, job_time, job_params, job_mod_dependency, job_allow_disable, job_status, job_next_runtime, job_last_runtime, job_last_status, job_description) VALUES (nextval('cron_job_seq'), 'CsvImportJob', 'CsvImportJob', '0 */15 * * * ?', NULL, NULL, 'Y', 'I', NULL, NULL, NULL, '00:15:00 Minutes:CSVs will be imported every 15 mins automatically');
ALTER TABLE package_issue_uom ADD COLUMN integration_uom_id character varying(100) UNIQUE;
ALTER TABLE manf_master ADD COLUMN integration_manf_id character varying(100) UNIQUE;
ALTER TABLE generic_name ADD COLUMN integration_generic_name_id character varying(100) UNIQUE;
ALTER TABLE service_groups ADD COLUMN integration_service_group_id character varying(100) UNIQUE;
ALTER TABLE service_sub_groups ADD COLUMN integration_service_sub_group_id character varying(100) UNIQUE;
ALTER TABLE store_item_controltype ADD COLUMN integration_control_type_id character varying(100) UNIQUE;
ALTER TABLE item_form_master ADD COLUMN integration_form_id character varying(100) UNIQUE;
ALTER TABLE item_insurance_categories ADD COLUMN integration_insurance_category_id character varying(100) UNIQUE;
ALTER TABLE item_groups ADD COLUMN integration_group_id character varying(100) UNIQUE;
ALTER TABLE item_sub_groups ADD COLUMN integration_subgroup_id character varying(100) UNIQUE;
ALTER TABLE store_category_master ADD COLUMN integration_category_id character varying(100) UNIQUE;