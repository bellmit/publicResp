-- liquibase formatted sql
-- changeset sanjana:depricate-request_handler_key

alter table u_user rename column request_handler_key to obsolete_request_handler_key;