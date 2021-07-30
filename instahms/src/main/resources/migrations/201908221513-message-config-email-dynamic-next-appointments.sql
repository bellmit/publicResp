-- liquibase formatted sql
-- changeset allabakash:message-config-email-dynamic-next-appointments

INSERT INTO message_config (message_configuration_id, message_type_id, param_name, param_value, 
param_description) VALUES (nextval('message_config_seq'::regclass),'email_next_day_appointment_reminder', 'status', 'Both', 'Appointment reminder status, pick only Book/Confirmed or Both appointments' )	;
INSERT INTO message_config (message_configuration_id, message_type_id, param_name, param_value, 
param_description) VALUES (nextval('message_config_seq'::regclass),'email_dynamic_appointment_reminder', 'status', 'Both', 'Appointment reminder status, pick only Book/Confirmed or Both appointments' )	; 