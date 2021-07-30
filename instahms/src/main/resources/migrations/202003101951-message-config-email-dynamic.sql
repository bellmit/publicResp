-- liquibase formatted sql
-- changeset manasaparam:removing-message-config-for-email-dynamic

DELETE FROM message_config WHERE message_type_id ='email_dynamic_appointment_reminder' AND param_name='buffer_hours';