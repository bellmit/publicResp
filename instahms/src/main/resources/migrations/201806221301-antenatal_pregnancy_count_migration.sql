-- liquibase formatted sql

-- changeset sonam009:antenatal-pregnancy-count-default-value

ALTER TABLE antenatal_main ALTER COLUMN pregnancy_count SET DEFAULT 1;