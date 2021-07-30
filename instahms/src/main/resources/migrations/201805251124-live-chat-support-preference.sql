-- liquibase formatted sql
-- changeset raj-nt:live_chat_support_preference

ALTER TABLE generic_preferences ADD COLUMN live_chat_support boolean NOT NULL DEFAULT false;
