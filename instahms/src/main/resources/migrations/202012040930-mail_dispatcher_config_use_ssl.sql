-- liquibase formatted sql
-- changeset rajendratalekar:add-column-use-ssl-response-message-dispatch-config

ALTER TABLE message_dispatcher_config ADD COLUMN use_ssl boolean default false;

UPDATE message_dispatcher_config set use_ssl = true, use_tls = false where protocol='smtp' and port_no=465;
UPDATE message_dispatcher_config set use_ssl = true, use_tls = true where protocol='smtp' and port_no=587;