-- liquibase formatted sql
-- changeset sirisharl:<commit-message-describing-this-database-change>
ALTER TABLE tests_prescribed ADD COLUMN cancel_reason text;