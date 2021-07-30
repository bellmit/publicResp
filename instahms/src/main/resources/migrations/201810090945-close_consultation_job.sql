-- liquibase formatted sql
-- changeset tejakilaru:close-consultation-job

INSERT INTO cron_job(job_group, job_name, job_time, job_allow_disable, 
	job_status, job_description) 
VALUES('AutoCloseConsultationJob', 'AutoCloseConsultationJob', '0 10 0 * * ?', 'Y', 'A', 
	'00:10:00 daily: closing consultations after x hours configured in clinical preferences');
