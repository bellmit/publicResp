-- liquibase formatted sql
-- changeset raeshmika:<drop-preference-enable-sms>

ALTER TABLE generic_preferences DROP COLUMN enable_sms;