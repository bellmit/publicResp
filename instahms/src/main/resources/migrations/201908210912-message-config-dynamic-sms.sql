-- liquibase formatted sql
-- changeset allabakash:message-config-dynamic-sms

INSERT INTO message_config (message_configuration_id, message_type_id, param_name, param_value, 
param_description) VALUES (nextval('message_config_seq'), 'sms_dynamic_appointment_reminder', 
'status', 'Confirmed', 'Appointment reminder status, while sending dynamic sms, it will pick Booked/Confirmed/Both smses.');