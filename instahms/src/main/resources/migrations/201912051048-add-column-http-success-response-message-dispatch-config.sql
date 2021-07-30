-- liquibase formatted sql
-- changeset rajendratalekar:add-column-http-success-response-message-dispatch-config

ALTER TABLE message_dispatcher_config ADD COLUMN http_success_response TEXT;
