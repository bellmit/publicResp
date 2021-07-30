-- liquibase formatted sql
-- changeset allabakash:message-config-next-day-sms

INSERT INTO message_config (message_configuration_id, message_type_id, param_name, param_value, 
param_description) VALUES (nextval('message_config_seq'), 'sms_next_day_appointment_reminder', 
'status', 'Confirmed', 'Appointment reminder status, pick only Booked/Confirmed or Both appointments.');

UPDATE message_config SET param_description = 'Appointment reminder status, pick only Booked/Confirmed or Both appointments.'
 WHERE message_type_id = 'sms_dynamic_appointment_reminder' AND param_name = 'status';
