-- liquibase formatted sql
-- changeset javalkarvinay:preference_for_last_name_required

ALTER TABLE registration_preferences ADD COLUMN last_name_required CHARACTER(1) DEFAULT 'N';
