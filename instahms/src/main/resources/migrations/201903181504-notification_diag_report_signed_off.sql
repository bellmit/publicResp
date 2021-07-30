-- liquibase formatted sql
-- changeset yashwantkumar:diag-signedoff-notification-db


insert into message_events(event_id, event_name, event_description)
values('diag_report_signedoff_notification', 'Diag Signed off report Notification', 'Event is used for triggering the Diag report signed off notification');

insert into message_types(message_type_id, message_type_name, message_type_description, message_body, event_id, message_mode, status, message_group, editability) 
values('notification_diag_report_signed_off', 'Diagnostic Reports Review', 'Signed-off  - Lab & Radiology Report', 'Notification Messages  which are send to Prescribing Doctor when the Lab and Radiology Reports are signed-off or Addendum is made available.', 'diag_report_signedoff_notification', 'NOTIFICATION', 'A', 'general', 'N');

insert into message_actions(message_action_id, message_action_name, message_action_type, allowed_actors, allowed_usage, options) 
values(8, 'Notification Diag Signed off report', 'custom_diag_notification', 'O', 1, 'Review');

insert into message_type_actions(message_type_id, message_action_mask, sender_override_mask)
values('notification_diag_report_signed_off', 8, 8);

insert into message_category(message_category_id, message_category_name, status)
values(nextval('message_category_seq'), 'Clinical', 'A');

update message_types set category_id = (select message_category_id from message_category where message_category_name = 'Clinical') where message_type_id = 'notification_diag_report_signed_off';
