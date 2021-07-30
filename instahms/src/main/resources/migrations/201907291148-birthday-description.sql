-- liquibase formatted sql
-- changeset sanjana.goyal:birthday-description

update message_types set message_type_description = 'Delight your patients by sending them birthday wishes.' where message_type_id in ('sms_birthday','email_birthday');