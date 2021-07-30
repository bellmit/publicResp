-- liquibase formatted sql
-- changeset sonam009:op-consultation-data-access-prefs

ALTER TABLE center_preferences ADD COLUMN op_consultation_data_access character varying(1) NOT NULL DEFAULT 'S';