-- liquibase formatted sql
-- changeset sanjana.goyal:waitlist-upgrade-message

insert into message_events values ('waitlist','waitlist','Event used for triggering waitlist upgrade messages');

insert into message_types(message_type_id,message_type_name,message_type_description,message_body,event_id,message_mode,status,category_id,editability,recipient_category,message_group_name) values ('sms_waitlist','Waitlist Upgrade (SMS)','Message is sent automatically to all the patients when the wait list no. is modified.','Your appointment on ${appointment_date} at ${appointment_time} has been allocated: wait list number ${waitlist}','waitlist','SMS','I',(select message_category_id from message_category where message_category_name ='Appointments'), 'A','Patient','Waitlist Upgrade');

insert into message_types(message_type_id,message_type_name,message_type_description,message_body,event_id,message_mode,status,category_id,editability,recipient_category,message_group_name, message_subject) values ('email_waitlist','Waitlist Upgrade (EMAIL)','Message is sent automatically to all the patients when the wait list no. is modified.','Your appointment on ${appointment_date} at ${appointment_time} has been allocated: wait list number ${waitlist}','waitlist','EMAIL','I',(select message_category_id from message_category where message_category_name ='Appointments'), 'A','Patient','Waitlist Upgrade', 'Waitlist Number Upgrade');
