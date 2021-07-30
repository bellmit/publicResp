-- liquibase formatted sql
-- changeset adeshatole:201901241153-discharge-for-pending-indent-warning

ALTER TABLE generic_preferences ADD COLUMN discharge_for_pending_indent character(1) DEFAULT 'A';