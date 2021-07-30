-- liquibase formatted sql
-- changeset javalkarvinay:remove_not_null_constraint_code_sets

ALTER TABLE code_sets ALTER COLUMN label DROP NOT NULL;
