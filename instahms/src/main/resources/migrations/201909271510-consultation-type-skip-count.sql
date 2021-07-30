-- liquibase formatted sql
-- changeset adeshatole:skip-count-preference-for-consultation-type

ALTER TABLE consultation_types ADD COLUMN skip_for_followup_count CHARACTER NOT NULL DEFAULT 'N';
