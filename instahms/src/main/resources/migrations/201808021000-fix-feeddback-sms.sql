-- liquibase formatted sql
-- changeset sanjana:fix-description-of-cron-job

update cron_job set job_description='00:19:00 daily send message to OP patients to give feedback for their visit with config 1' where job_name='FeedbackSMSJob_1';
update cron_job set job_description='00:10:00 daily send message to OP patients to give feedback for their visit with config 2' where job_name='FeedbackSMSJob_2';