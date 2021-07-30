-- liquibase formatted sql
-- changeset sanjana:feedback-sms

INSERT into cron_job (job_group,job_name,job_time,job_allow_disable, job_status,job_description,job_params) values ('FeedbackSMSJob','FeedbackSMSJob_1','0 0 19 * * ?','Y','I','Job to trigger sending of feedback messsage with config 1',1);

INSERT into cron_job (job_group,job_name,job_time,job_allow_disable, job_status,job_description,job_params) values ('FeedbackSMSJob','FeedbackSMSJob_2','0 0 10 * * ?','Y','I','Job to trigger sending of feedback messsage with config 2',2);

ALTER TABLE doctors ADD COLUMN send_feedback_sms boolean;
update doctors set send_feedback_sms= true;

INSERT into message_events values ('feedback_reminder','Feedback Message','Event used for triggering SMS to patients for doctor feedback');

INSERT into message_types (message_type_id, message_type_name, message_type_description, message_body, event_id, message_mode, message_group, editability, status) values ('sms_feedback_reminder','Feedback Message', 'Message to ask OP patients to give feedback (on the link provided in SMS) for their visit. One can configure using the message config link, the visit date time range for which the feedback sms will go.','Dear ${patient_name}, Thanks for giving us an opportunity to serve you ${day}. The team at ${center_name} would love to hear from you. Your feedback will help us serve you better. Kindly share your feedback by clicking on ${url}.','feedback_reminder','SMS','general','I','I');

insert into insta_integration (integration_name ,status, environment) values ('center_practo_profile','I','PROD');

insert into message_config (message_type_id,param_name,param_value,param_description) values ('sms_feedback_reminder', 'start_time_1', '00:00', 'Start time of first cron');

insert into message_config (message_type_id,param_name,param_value,param_description) values ('sms_feedback_reminder','end_time_1','14:00','End time of first cron');

insert into message_config (message_type_id,param_name,param_value,param_description) values ('sms_feedback_reminder','day_1','0','Day of first cron');

insert into message_config (message_type_id,param_name,param_value,param_description) values ('sms_feedback_reminder', 'start_time_2','14:01', 'Start time of second cron');

insert into message_config (message_type_id,param_name,param_value,param_description) values ('sms_feedback_reminder','end_time_2','23:59','End time of second cron');

insert into message_config (message_type_id,param_name,param_value,param_description) values ('sms_feedback_reminder','day_2','1','Day of second cron');

alter table center_integration_details add column param varchar(200);