-- liquibase formatted sql
-- changeset sanjana.goyal:message-config-replica

insert into message_config (message_type_id, param_name ,param_value,param_description) select (case when message_type_id ilike 'sms_%' then REPLACE(message_type_id, 'sms', 'email') else  REPLACE(message_type_id, 'email', 'sms') end), 
param_name ,param_value,param_description from message_config where message_type_id in (select message_type_id from message_types where recipient_category='Patient' and message_type_id !='email_diag_report');