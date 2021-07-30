-- liquibase formatted sql
-- changeset tejakilaru:test_validity_master_changes

ALTER TABLE diagnostics ADD COLUMN result_validity_period integer;
ALTER TABLE diagnostics ADD COLUMN result_validity_period_units character(1);
