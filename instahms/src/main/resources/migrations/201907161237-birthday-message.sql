-- liquibase formatted sql
-- changeset sanjana.goyal:birthday-message

insert into message_events values ('wish_birthday', 'Wish happy birthday', 'Wish happy birthday');

insert into message_category (message_category_name,status) select 'General' , 'A' where not exists (select * from message_category where message_category_name='General');

insert into message_types (message_type_id,message_type_name,message_type_description, message_body,event_id, message_mode, status, category_id, message_group_name, recipient_category, message_group) values ('sms_birthday', 'Birthday SMS', 'trigger a birthday wish sms daily at 12:15','Wishing you a happy birthday, a wonderful year and success in all you do.','wish_birthday', 'SMS','I', (select message_category_id from message_category where message_category_name= 'General') , 'Birthday', 'Patient','general');

insert into message_types (message_type_id, message_type_name, message_type_description, message_subject, message_body, event_id, message_mode, status,category_id, message_group_name, recipient_category, message_group) values ('email_birthday', 'Birthday Email', 'trigger a birthday wish email daily at 12:15','Happy Birthday','Wishing you a happy birthday, a wonderful year and success in all you do.','wish_birthday', 'EMAIL','I', (select message_category_id from message_category where message_category_name= 'General') , 'Birthday', 'Patient', 'general');

insert into cron_job (job_group,job_name,job_time,job_status,job_description, job_allow_disable) values ('BirthdayMessageJob','BirthdayMessageJob','0 15 12 * * ?','A','Triggers birthday sms daily at 12:15','Y');