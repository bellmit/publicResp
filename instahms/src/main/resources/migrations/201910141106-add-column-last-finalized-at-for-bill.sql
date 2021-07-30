-- liquibase formatted sql
-- changeset rajendratalekar:<commit-message-describing-this-database-change>

ALTER TABLE bill ADD COLUMN last_finalized_at timestamp without time zone;
