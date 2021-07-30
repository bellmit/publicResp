-- liquibase formatted sql
-- changeset sanjana:<commit-message-describing-this-database-change>

alter table message_types add column confidential character(1) default 'N';
update message_types set confidential='Y' where message_type_id in ('sms_enable_mobile_access','email_enable_mobile_access');