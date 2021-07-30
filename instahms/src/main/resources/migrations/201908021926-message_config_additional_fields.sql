-- liquibase formatted sql
-- changeset vinaykumarjavalkar:Added additional fields for 2waysms message configuration

ALTER TABLE message_dispatcher_config ADD COLUMN protocol_template_type VARCHAR(10);
ALTER TABLE message_dispatcher_config ADD COLUMN http_method VARCHAR(10);
ALTER TABLE message_dispatcher_config ADD COLUMN http_url TEXT;
ALTER TABLE message_dispatcher_config ADD COLUMN http_header TEXT;
ALTER TABLE message_dispatcher_config ADD COLUMN http_body TEXT;
