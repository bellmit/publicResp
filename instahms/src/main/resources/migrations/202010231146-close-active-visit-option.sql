-- liquibase formatted sql
-- changeset asif:Adding-close-active-visit

ALTER TABLE registration_preferences ADD COLUMN close_previous_active_visit character(1) NOT NULL DEFAULT 'Y';

