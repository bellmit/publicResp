-- liquibase formatted sql
-- changeset riyapoddar-13:add-section-lock-period

ALTER TABLE clinical_preferences ADD COLUMN section_lock_period smallint DEFAULT 15;
