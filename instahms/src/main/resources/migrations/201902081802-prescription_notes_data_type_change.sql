-- liquibase formatted sql
-- changeset sonam009:prescription_notes_data_type_change

ALTER TABLE doctor_consultation ALTER COLUMN prescription_notes TYPE text;
