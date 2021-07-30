-- liquibase formatted sql
-- changeset adeshatole:add-prescription-expiry

ALTER TABLE generic_preferences ADD COLUMN prescription_validity NUMERIC DEFAULT 30;